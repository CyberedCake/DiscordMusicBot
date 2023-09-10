package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.settings.SettingSubCommand;
import net.cybercake.discordmusicbot.utilities.Preconditions;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Queue implements Serializable {

    private final Guild guild;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final GuildSettings settings;

    private AudioChannelUnion voiceChannel;
    private TextChannel textChannel;

    private final List<SkipVote> skipVotes = new ArrayList<>();

    protected Queue(AudioPlayerManager audioPlayerManager, Guild guild, AudioChannelUnion voiceChannel, TextChannel textChannel) {
        this.settings = SettingSubCommand.doesExist_elseCreate(guild);

        this.guild = guild;
        this.voiceChannel = voiceChannel;
        this.textChannel = textChannel;

        this.audioManager = guild.getAudioManager();
        this.audioManager.openAudioConnection(voiceChannel);
        this.audioManager.setSelfDeafened(true);

        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayerManager.createPlayer();

        this.trackScheduler = new TrackScheduler(this.guild, this.audioPlayer);

        this.audioPlayer.addListener(this.trackScheduler);
    }

    public Guild getGuild() { return this.guild; }
    public AudioPlayer getAudioPlayer() { return this.audioPlayer; }
    public TrackScheduler getTrackScheduler() { return this.trackScheduler; }
    public GuildSettings getSettings() { return this.settings; }

    public AudioChannelUnion getVoiceChannel() { return this.voiceChannel; }
    public TextChannel getTextChannel() { return this.textChannel; }

    public void setVoiceChannel(AudioChannelUnion voiceChannel, IReplyCallback callback) {
        this.voiceChannel = voiceChannel;
        this.audioManager.closeAudioConnection();
        Thread minorDelay = new Thread(() -> {
            try {
                Thread.sleep(400L);
                this.audioManager.openAudioConnection(voiceChannel);
                Thread.sleep(200L);
                callback.getHook().editOriginal("You moved the music bot into your voice channel, <#" + voiceChannel.getId() + ">!").queue();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to rejoin voice chat", exception);
            }
        });
        minorDelay.start();
    }
    public void setTextChannel(TextChannel textChannel) { this.textChannel = textChannel; }

    public void loadAndPlay(final User requestedBy, String trackUrl, final SlashCommandInteractionEvent event, Command command, boolean startNow, boolean shuffle) {
        if(command.requiresDjRole() && Command.requireDjRole(event, event.getMember())) {
            if(getTrackScheduler().getQueue().isEmpty())
                destroy();
            return; // one last check for first usage reasons
        }

        String trackUrlCheckEffectiveFinal = trackUrl; // required because needs an effective final variable
        if(Preconditions.checkThrows(() -> new URL(trackUrlCheckEffectiveFinal), MalformedURLException.class))
            trackUrl = "ytsearch:" + trackUrl;
        final String searchQuery = trackUrl; // same deal here, needs effective final variable for some functions to work
        this.audioPlayerManager.loadItem(searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(startNow) playNow(track);
                else play(track);

                track.setUserData(requestedBy);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setThumbnail(TrackUtils.getThumbnailLinkFor(track));
                builder.setColor(new Color(0, 211, 16));
                builder.addField("Enqueued Track:", (startNow ? "`#1`" : "`#" + trackScheduler.getQueue().size() + "`") + " - [" + track.getInfo().title + "](https://www.youtube.com/watch?v=" + track.getIdentifier() + ")", true);
                builder.addField("Requested By:", requestedBy.getAsMention(), true);
                builder.addField("Duration:", "`" + TrackUtils.getFormattedDuration(track.getDuration()) + "`", true);

                event.getHook().editOriginalEmbeds(builder.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(playlist.isSearchResult()) {
                    trackLoaded(playlist.getTracks().get(0));
                    return;
                }

                List<AudioTrack> tracks = playlist.getTracks();
                if(shuffle)
                    Collections.shuffle(tracks);
                tracks.forEach(track -> {
                    track.setUserData(requestedBy);
                    if(startNow) trackScheduler.queueTop(track);
                    else trackScheduler.queue(track);
                });

                EmbedBuilder builder = new EmbedBuilder();
                builder.setThumbnail(TrackUtils.getThumbnailLinkFor(tracks.get(0)));
                if(shuffle)
                    builder.setDescription("*Added playlist tracks in a random order.*");
                builder.setColor(new Color(0, 211, 16));
                builder.addField("Enqueued Playlist:", playlist.getName(), true);
                builder.addField("Requested By:", requestedBy.getAsMention(), true);
                builder.addField("Items in Playlist:", "`" + playlist.getTracks().size() + "`", true);

                event.getHook().editOriginalEmbeds(builder.build()).queue();
            }

            @Override
            public void noMatches() {
                event.getHook().editOriginal("Failed to find any track named `" + searchQuery.replace("ytsearch:", "") + "`").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getHook().editOriginal("Could not play that track. Try again later! `" + exception.getMessage() + "`").queue();
            }
        });
    }

    private void play(AudioTrack track) {
        connectFirst();
        this.trackScheduler.queue(track);
    }

    private void playNow(AudioTrack track) {
        connectFirst();
        this.trackScheduler.queueTop(track);
    }

    private void connectFirst() {
        if(this.audioManager.isConnected()) return;

        this.audioManager.openAudioConnection(this.voiceChannel);
        if(this.voiceChannel instanceof StageChannel stage)
            stage.requestToSpeak().queueAfter(400, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        Main.queueManager.removeQueue(this);
        this.getAudioPlayer().stopTrack();
        this.getTrackScheduler().getQueue().clear();
        this.audioManager.closeAudioConnection();
        this.audioPlayer.destroy();
    }


    public SkipSongManager getSkipSongManager() { return new SkipSongManager(); }
    public class SkipSongManager {
        protected void clearSkipVoteQueue() {
            skipVotes.clear();
        }

        public boolean hasMemberSkipVoted(Member member) {
            return skipVotes.stream().filter(vote -> vote.member().getIdLong() == member.getIdLong()).findFirst().orElse(null) != null;
        }

        public void addMemberToSkipVote(Member member) {
            if(hasMemberSkipVoted(member))
                throw new IllegalArgumentException("That member has already voted to skip at some point in the past.");
            skipVotes.add(new SkipVote(member, System.currentTimeMillis(), audioPlayer.getPlayingTrack()));
        }

        public List<Member> getUsersWhoCanSkip() { return getVoiceChannel().getMembers()
                .stream()
                .filter(member -> !member.getUser().isBot())
                .filter(member -> getVoiceChannel().getType() == ChannelType.STAGE
                        && getVoiceChannel().asStageChannel().getStageInstance() != null
                        && Objects.requireNonNull(getVoiceChannel().asStageChannel().getStageInstance()).getSpeakers().contains(member)
                )
                .toList();
        }

        public int getMembersWantingSkip() { return skipVotes.size(); }

        /**
         * This percentage is already multiplied by 100
         */
        public int getMembersWantingSkipPercent() {
            return Math.round(Float.parseFloat(String.valueOf(this.getMembersWantingSkip())) / Float.parseFloat(String.valueOf(this.getMaxNeededToSkip()))*100);
        }

        public int getMaxNeededToSkip() {
            return Math.round((Float.parseFloat(String.valueOf(this.getUsersWhoCanSkip().size()))* (Main.SKIP_VOTE_PERCENTAGE)));
        }

        public void checkSkipProportion() {
            if(getMembersWantingSkipPercent() < 100) return;
            trackScheduler.nextTrack();
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "guild=" + guild +
                ", voiceChannel=" + voiceChannel +
                ", textChannel=" + textChannel +
                ", audioPlayerManager=" + audioPlayerManager +
                ", audioManager=" + audioManager +
                ", audioPlayer=" + audioPlayer +
                ", trackScheduler=" + trackScheduler +
                ", skipVotes=" + skipVotes +
                '}';
    }
}
