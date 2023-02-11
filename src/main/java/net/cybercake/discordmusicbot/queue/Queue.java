package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class Queue {

    private final Guild guild;
    private final VoiceChannel voiceChannel;
    private final AudioPlayerManager audioPlayerManager;
    private final AudioManager audioManager;
    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    protected Queue(AudioPlayerManager audioPlayerManager, Guild guild, VoiceChannel voiceChannel) {
        this.guild = guild;
        this.voiceChannel = voiceChannel;

        this.audioManager = Main.guild.getAudioManager();
        this.audioManager.openAudioConnection(voiceChannel);

        this.audioPlayerManager = audioPlayerManager;
        this.player = audioPlayerManager.createPlayer();

        this.scheduler = new TrackScheduler(this.player);

        this.player.addListener(this.scheduler);
    }

    public Guild getGuild() { return this.guild; }

    public AudioPlayerSendHandler getSendHandler() { return new AudioPlayerSendHandler(this.player); }

    public void loadAndPlay(final TextChannel textChannel, final User requestedBy, final String trackUrl) {
        this.audioPlayerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
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
        this.scheduler.queue(track);
        this.scheduler.nextTrack();
    }

    private void skipTrack(TextChannel channel) {
        this.scheduler.nextTrack();
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
