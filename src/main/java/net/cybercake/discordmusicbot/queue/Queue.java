package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class Queue {

    private final TrackScheduler scheduler;
    private final LinkedList<AudioTrack> literalQueue;

    private int currentIndex;

    Queue(TrackScheduler scheduler) {
        this.scheduler = scheduler;
        this.literalQueue = new LinkedList<>();
        this.currentIndex = 0;
    }

    public TrackScheduler getScheduler() { return this.scheduler; }

    public LinkedList<AudioTrack> getLiteralQueue() { return this.literalQueue; }
    public int getCurrentIndex() { return this.currentIndex; }
    public AudioTrack getCurrentItem() { return this.getLiteralQueue().get(this.getCurrentIndex() - 1); }

    public boolean isEmpty() { return this.getLiteralQueue().isEmpty(); }

    public boolean isAtEnd() { return isIndexPastEnd(this.getCurrentIndex()); }
    public boolean isIndexPastEnd(int index) { return index >= getQueueEndIndex(); }
    public int getQueueEndIndex() { return this.getLiteralQueue().size(); }

    public boolean isCurrent(AudioTrack testTrack) { return testTrack == getCurrentItem(); }

    public int getIndexOf(AudioTrack searchTrack) {
        int index = 0;
        for (AudioTrack track : this.getLiteralQueue()) {
            if (track == searchTrack) break;
            index ++;
        }
        if (index >= this.getLiteralQueue().size()) index = 0;
        return index;
    }

    public void addToQueue(AudioTrack track) { this.addToQueue(track, false); }
    public void addToQueue(AudioTrack track, boolean asNext) {
        if(asNext)
            this.literalQueue.add(currentIndex, track);
        else this.literalQueue.add(track);
    }

    public void playNextTrack(boolean interrupt) {
        if(this.scheduler.audioPlayer.startTrack(this.literalQueue.get(this.currentIndex).makeClone(), !interrupt))
            this.currentIndex++;
    }

    public void toIndex(int index) {
        this.currentIndex = index;
    }

    public void randomizeOrder() {
        AudioTrack current = this.getCurrentItem();
        Collections.shuffle(literalQueue, new Random(System.currentTimeMillis()));
        this.literalQueue.add(0, current);
        this.currentIndex = 1;
    }

    public AudioTrack remove(int position) { return this.literalQueue.remove(position); }

    public AudioTrack getTrackAt(int position) {
        return this.literalQueue.get(position);
    }

    public void destroy() {
        this.literalQueue.clear();
    }


}
