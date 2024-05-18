package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.GuildSettings;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.settings.SettingSubCommand;
import net.cybercake.discordmusicbot.queue.seek.SongQueueSeekManager;
import net.cybercake.discordmusicbot.utilities.Preconditions;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.commons.collections4.map.LinkedMap;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MusicPlayer implements Serializable {

    public static String FILE_IDENTIFIER = UUID.randomUUID().toString();

    private final Guild guild;
    private final AudioManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final GuildSettings settings;

    private AudioChannelUnion voiceChannel;
    private TextChannel textChannel;

    final AudioPlayerManager audioPlayerManager;

    private final SongQueueSeekManager seekManager;

    protected MusicPlayer(AudioPlayerManager audioPlayerManager, Guild guild, AudioChannelUnion voiceChannel, TextChannel textChannel) {
        this.settings = SettingSubCommand.doesExist_elseCreate(guild);

        this.guild = guild;
        this.voiceChannel = voiceChannel;
        this.textChannel = textChannel;

        this.audioManager = guild.getAudioManager();
        this.audioManager.openAudioConnection(voiceChannel);
        this.audioManager.setSelfDeafened(true);

        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayerManager.createPlayer();

        this.seekManager = SongQueueSeekManager.as(this);

        this.trackScheduler = new TrackScheduler(this.guild, this.audioPlayer);

        this.audioPlayer.addListener(this.trackScheduler);
    }

    public Guild getGuild() { return this.guild; }
    public AudioPlayer getAudioPlayer() { return this.audioPlayer; }
    public TrackScheduler getTrackScheduler() { return this.trackScheduler; }
    public GuildSettings getSettings() { return this.settings; }

    public SongQueueSeekManager getSeekManager() { return this.seekManager; }

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

    public void loadAndPlay(TrackLoadSettings settings) {
        if(settings.getParentCommand().requiresDjRole() && Command.requireDjRole(settings.getParentEvent(), settings.getParentEvent().getMember())) {
            return; // one last check for first usage reasons
        }

        if(settings.getFile() != null) {
            settings.change((builder) -> builder.trackUrl(FILE_IDENTIFIER));
            this.audioPlayerManager.loadItem(FILE_IDENTIFIER, new TrackLoadStatus(this, settings));
            return;
        }

        if(settings.getTrackUrl() != null) {
            String trackUrl = settings.getTrackUrl();
            final String checkUrl = trackUrl; // required for lambda below: () -> new URL(checkUrl) requires a final variable
            if(Preconditions.checkThrows(() -> new URL(checkUrl), MalformedURLException.class))
                trackUrl = "ytsearch:" + trackUrl;
            final String searchQuery = trackUrl; // same deal here, needs effective final variable for some functions to work
            this.audioPlayerManager.loadItem(searchQuery, new TrackLoadStatus(this, settings));
        }
    }

    void play(AudioTrack track, boolean asNext) {
        connectFirst();
        this.trackScheduler.queue.addToQueue(track, asNext);
        this.trackScheduler.queue.playNextTrack(false);
    }

    private void connectFirst() {
        if(this.audioManager.isConnected()) return;

        this.audioManager.openAudioConnection(this.voiceChannel);
        if(this.voiceChannel instanceof StageChannel stage)
            stage.requestToSpeak().queueAfter(400, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        Main.musicPlayerManager.removeMusicPlayer(this);
        this.getAudioPlayer().stopTrack();
        this.getTrackScheduler().destroy();
        this.audioManager.closeAudioConnection();
        this.audioPlayer.destroy();
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
                ", skipVotes=" + this.seekManager +
                '}';
    }

    public Map<String, ?> data() {
        return new LinkedMap<>()
        {{
            put("hash", getGuild().getName().hashCode()); // 0
            put("id", getAudioPlayer().getPlayingTrack().getInfo().identifier); // 1
            put("song_at", getAudioPlayer().getPlayingTrack().getPosition()); // 2
            put("song_duration", getAudioPlayer().getPlayingTrack().getDuration()); // 3
            put("queue_size", getTrackScheduler().getQueue().getLiteralQueue().size()); // 4
            put("active_users", getVoiceChannel().getMembers().stream().filter(member -> !member.getUser().isBot()).toList().size()); // 5
        }};
    }
}
