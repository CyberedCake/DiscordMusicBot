package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Preconditions;
import net.cybercake.discordmusicbot.generalutils.Sort;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class Queue implements Serializable {

    private final Guild guild;
    private final VoiceChannel voiceChannel;
    private final TextChannel textChannel;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;

    private final List<SkipVote> skipVotes = new ArrayList<>();

    protected Queue(AudioPlayerManager audioPlayerManager, Guild guild, VoiceChannel voiceChannel, TextChannel textChannel) {
        this.guild = guild;
        this.voiceChannel = voiceChannel;
        this.textChannel = textChannel;

        this.audioManager = guild.getAudioManager();
        this.audioManager.openAudioConnection(voiceChannel);

        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayerManager.createPlayer();

        this.trackScheduler = new TrackScheduler(this.guild, this.audioPlayer);

        this.audioPlayer.addListener(this.trackScheduler);
    }

    public Guild getGuild() { return this.guild; }
    public AudioPlayer getAudioPlayer() { return this.audioPlayer; }
    public TrackScheduler getTrackScheduler() { return this.trackScheduler; }

    public VoiceChannel getVoiceChannel() { return this.voiceChannel; }
    public TextChannel getTextChannel() { return this.textChannel; }

    public void loadAndPlay(final TextChannel textChannel, final User requestedBy, String trackUrl, final SlashCommandInteractionEvent event) {
        String trackUrlCheckEffectiveFinal = trackUrl; // required because needs an effective final variable
        if(Preconditions.checkThrows(() -> new URL(trackUrlCheckEffectiveFinal), MalformedURLException.class))
            trackUrl = "ytsearch:" + trackUrl;
        final String searchQuery = trackUrl; // same deal here, needs effective final variable for some functions to work
        this.audioPlayerManager.loadItem(searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(track);
                track.setUserData(requestedBy);
                event.getHook().editOriginal("Enqueued `" + track.getInfo().title + "` (by `" + requestedBy.getName() + "#" + requestedBy.getDiscriminator() + "`) in position `" + trackScheduler.getQueue().size() + "`").queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(searchQuery.contains("search")) {
                    trackLoaded(playlist.getTracks().get(0));
                    return;
                }

                AudioTrack firstTrack = playlist.getSelectedTrack();
                if(firstTrack == null) firstTrack = playlist.getTracks().get(0);

                // play(firstTrack);

                playlist.getTracks().forEach(track -> {
                    track.setUserData(requestedBy);
                    trackScheduler.queue(track);
                });

                event.getHook().editOriginal("Added `" + playlist.getTracks().size() + "` tracks to the queue.").queue();
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

    private void connectFirst() {
        if(audioManager.isConnected()) return;

        this.audioManager.openAudioConnection(this.voiceChannel);
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

        public List<Member> getHumanVCMembers() { return getVoiceChannel().getMembers()
                .stream()
                .filter(member -> !member.getUser().isBot())
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
            return Math.round((Float.parseFloat(String.valueOf(this.getHumanVCMembers().size()))* (Main.SKIP_VOTE_PERCENTAGE)));
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
