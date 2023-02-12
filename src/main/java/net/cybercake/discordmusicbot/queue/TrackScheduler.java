package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Pair;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;

    public enum Repeating { FALSE, REPEATING_SONG, REPEATING_ALL }
    private Repeating repeating;

    private @Nullable Pair<TextChannel, Long> message;

    public TrackScheduler(Guild guild, AudioPlayer audioPlayer) {
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
        this.repeating = Repeating.FALSE;

        this.message = null;
    }

    @SuppressWarnings({"all"})
    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true))
            queue.offer(track);
    }

    public void nextTrack() {
        audioPlayer.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        try {
            Thread sendNowPlayingMessage = new Thread(() -> {
                try {
                    Thread.sleep(600L); // delay because information that is set after this method finishes is required inside the embed
                    this.message = Embeds.sendSongPlayingStatus(track, this.guild);
                } catch (Exception exception) {
                    throw new IllegalStateException("Failed to send now playing message to guild " + guild.getId() + " (" + guild.getName() + ")", exception);
                }
            });
            sendNowPlayingMessage.start();
        } catch (Exception exception) {
            Log.error("Failed to start track for guild " + guild.getId() + " (" + guild.getName() + ")", exception);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(this.message != null) { // delete previous message if it exists
            this.message.getFirstItem().deleteMessageById(this.message.getSecondItem()).queue();
            this.message = null;
        }
        Main.queueManager.getGuildQueue(this.guild).clearSkipVoteQueue();
        if(endReason.mayStartNext) {
            switch(this.repeating) {
                case REPEATING_SONG -> player.startTrack(track.makeClone(), false);
                case REPEATING_ALL, FALSE -> nextTrack();
            }
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return this.queue;
    }

    public Repeating isRepeating() { return this.repeating; }
    public void setRepeating(Repeating repeating) { this.repeating = repeating; }
}
