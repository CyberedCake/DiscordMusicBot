package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.AudioPlayerSendHandler;
import net.cybercake.discordmusicbot.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class Queue {

    private final Guild guild;
    private final VoiceChannel voiceChannel;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager audioManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler trackScheduler;
    private final List<Song> playList;

    protected Queue(AudioPlayerManager audioPlayerManager, Guild guild, VoiceChannel voiceChannel) {
        this.guild = guild;
        this.voiceChannel = voiceChannel;

        this.audioManager = Main.guild.getAudioManager();
        this.audioManager.openAudioConnection(voiceChannel);

        this.playList = new ArrayList<>();
        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayerManager.createPlayer();

        this.trackScheduler = new TrackScheduler(this.audioPlayer);

        this.audioPlayer.addListener(this.trackScheduler);
    }

    public Guild getGuild() { return this.guild; }
    public VoiceChannel getVoiceChannel() { return this.voiceChannel; }
    public AudioPlayerManager getAudioPlayerManager() { return this.audioPlayerManager; }
    public AudioManager getAudioManager() { return this.audioManager; }
    public AudioPlayer getAudioPlayer() { return this.audioPlayer; }
    public TrackScheduler getTrackScheduler() { return this.trackScheduler; }
    public AudioPlayerSendHandler getSendHandler() { return new AudioPlayerSendHandler(this.audioPlayer); }

    public void addSong(String identifier, User requestedBy) {
        Song song = new Song(this, identifier);

        this.playList.add(song);
        this.audioPlayerManager.loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                getAudioPlayer().playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });

    }

    public void loadAndPlay(final TextChannel textChannel, final String trackUrl) {
        Queue queue = Main.queueManager.getQueueFor(textChannel.getGuild());

        this.audioPlayerManager.loadItemOrdered(this, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                textChannel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if(firstTrack == null) firstTrack = playlist.getTracks().get(0);

                textChannel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(firstTrack);
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                textChannel.sendMessage("Could not play that track. Try again later! " + exception.getMessage()).queue();
            }
        });
    }

    private void play(AudioTrack track) {
        connectFirst();
        this.trackScheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        this.trackScheduler.nextTrack();
        channel.sendMessage("Skipped to next track.").queue();
    }

    private void connectFirst() {
        if(audioManager.isConnected()) return;

        this.audioManager.openAudioConnection(this.voiceChannel);
    }

//    public void removeSong(String title) {
//        this.playList.remove(
//                this.playList
//                        .stream()
//                        .filter(song -> song.getTitle().equalsIgnoreCase(title))
//                        .findFirst()
//                        .orElseThrow(() -> new IllegalArgumentException("Parameter 'title' is not a song in the queue!"))
//        );
//    }

}
