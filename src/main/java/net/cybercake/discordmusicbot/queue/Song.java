package net.cybercake.discordmusicbot.queue;

public class Song {

    private final Queue parentQueue;
    private final String trackUrl;

    public Song(Queue parentQueue, String trackUrl) {
        this.parentQueue = parentQueue;
        this.trackUrl = trackUrl;
    }

    public Queue getParentQueue() { return this.parentQueue; }
    public String getTrackUrl() { return this.trackUrl; }

}
