package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;

    public enum Repeating { FALSE, REPEATING_SONG, REPEATING_ALL }
    private Repeating repeating;

    public TrackScheduler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.repeating = Repeating.FALSE;
    }

    @SuppressWarnings({"all"})
    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        audioPlayer.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext) {
            switch(this.repeating) {
                case REPEATING_SONG -> player.startTrack(track.makeClone(), false);
                case REPEATING_ALL, FALSE -> nextTrack();
            }
        }
    }

    public Repeating isRepeating() { return this.repeating; }
    public void setRepeating(Repeating repeating) { this.repeating = repeating; }
}
