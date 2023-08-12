package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.list.Resume;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.utilities.Pair;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

public class TrackScheduler extends AudioEventAdapter {

    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final Stack<AudioTrack> queue;

    public enum Repeating { FALSE, REPEATING_SONG, REPEATING_ALL }
    private Repeating repeating;

    public int TRACK_EXCEPTION_MAXIMUM_REPEATS = 4;
    private String trackExceptionId;
    private int trackExceptionRepeats;

    private @Nullable Pair<TextChannel, Long> message;

    public TrackScheduler(Guild guild, AudioPlayer audioPlayer) {
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.queue = new Stack<>();
        this.repeating = Repeating.FALSE;

        this.message = null;
    }

    @SuppressWarnings({"all"})
    public void queue(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true))
            queue.push(track);
    }

    public void queueTop(AudioTrack track) {
        if(!audioPlayer.startTrack(track, true))
            queue.add(0, track);
    }

    public void pause(boolean pause) { this.audioPlayer.setPaused(pause); }
    public boolean pause() { return this.audioPlayer.isPaused(); }

    public void nextTrack() {
        if(queue.isEmpty()) {
            endQueue(Main.queueManager.getGuildQueue(guild)); return;
        }
        AudioTrack nextTrack = queue.firstElement();
        queue.remove(nextTrack);

        try {
            audioPlayer.startTrack(nextTrack, false);
            this.trackExceptionRepeats = 0;
        } catch (IllegalStateException illegalStateException) {
            Log.error("Failed in starting next track", illegalStateException);
            if(!illegalStateException.getMessage().contains("Cannot play the same instance")) return;
            endQueue(Main.queueManager.getGuildQueue(this.guild));
        }
    }

    public void shuffle() {
        if(queue.isEmpty()) throw new IllegalStateException("Cannot shuffle a queue that is less than one item.");

        Collections.shuffle(queue, new Random(System.currentTimeMillis()));
    }

    public enum ToDoWithOld {
        NOTHING, DELETE, EDIT
    }

    @SuppressWarnings({"all"})
    public void sendNowPlayingStatus(AudioTrack track, ToDoWithOld toDoWithOld) {
        try {
            if(toDoWithOld == ToDoWithOld.DELETE)
                deleteOldNowPlayingStatus();
            Thread sendNowPlayingMessage = new Thread(() -> {
                try {
                    Thread.sleep(600L); // delay because information that is set after this method finishes is required inside the embed
                    if(!Main.queueManager.checkQueueExists(this.guild)) return;
                    this.message = Embeds.sendSongPlayingStatus(
                            track,
                            this.guild,
                            (toDoWithOld == ToDoWithOld.EDIT ? Objects.requireNonNullElse(this.message, new Pair<TextChannel, Long>(null, -1L)).getSecondItem() : -1L)
                    );
                } catch (Exception exception) {
                    throw new IllegalStateException("Failed to send now playing message to guild " + guild.getId() + " (" + guild.getName() + ")", exception);
                }
            });
            sendNowPlayingMessage.start();
        } catch (Exception exception) {
            Log.error("Failed to start track for guild " + guild.getId() + " (" + guild.getName() + ")", exception);
        }
    }

    public void deleteOldNowPlayingStatus() {
        if(this.message != null) { // delete previous message if it exists
            this.message.getFirstItem().deleteMessageById(this.message.getSecondItem()).queue();
            this.message = null;
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Pair<User, Exception> data = TrackUtils.deserializeUserData(track.getUserData());
        if(data != null && data.getSecondItem() != null) {
            Log.info(">> SONG START DELAYED ... EXCEPTION FOUND <<");
            return;
        }
        sendNowPlayingStatus(track, ToDoWithOld.NOTHING);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason == AudioTrackEndReason.LOAD_FAILED && this.trackExceptionRepeats < TRACK_EXCEPTION_MAXIMUM_REPEATS)
            return;
        Resume.removePauseNickname(this.guild);
        deleteOldNowPlayingStatus();
        if(!Main.queueManager.checkQueueExists(this.guild)) return;
        Queue queueMain = Main.queueManager.getGuildQueue(this.guild);
        queueMain.getSkipSongManager().clearSkipVoteQueue();
        if(queue.isEmpty()) {
            endQueue(queueMain); return;
        }
        if(endReason.mayStartNext) {
            switch(this.repeating) {
                case REPEATING_SONG -> {
                    player.startTrack(track.makeClone(), false);
                    this.trackExceptionRepeats = 0;
                }
                case REPEATING_ALL, FALSE -> nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Log.error("An error occurred while playing a track", exception);
        this.trackExceptionRepeats++;
        if(trackExceptionRepeats <= TRACK_EXCEPTION_MAXIMUM_REPEATS) {
            Thread thread = new Thread(() -> {
                try {
                    int delay = 5 * 1000;
                    Thread.sleep(delay);
                    AudioTrack newTrack = track.makeClone();
                    newTrack.setUserData(new Pair<User, Exception>(TrackUtils.deserializeUserData(track.getUserData()).getFirstItem(), exception));
                    audioPlayer.startTrack(newTrack, false);
                    Log.info("Track failed, retrying in 5 seconds, tried " + trackExceptionRepeats + " out of " + TRACK_EXCEPTION_MAXIMUM_REPEATS);
                    if(this.message != null)
                        this.message.getFirstItem().editMessageById(this.message.getSecondItem(), "**Track failed.** Retrying <t:" + ((System.currentTimeMillis() + delay) / 1000) + ":R>. Tried " + trackExceptionRepeats + ", maximum " + TRACK_EXCEPTION_MAXIMUM_REPEATS + ".").queue();
                } catch (Exception exception1) {
                    throw new IllegalStateException("Failed to rate-limit self", exception1);
                }
            });
            thread.start();
            return;
        }
        TextChannel channel = Main.queueManager.getGuildQueue(guild).getTextChannel();
        if(this.message != null) { // delete previous message if it exists
            if(channel == null) channel = this.message.getFirstItem();
            this.message.getFirstItem().deleteMessageById(this.message.getSecondItem()).queue();
            this.message = null;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("An error occurred playing `" + track.getInfo().title + "`");
        builder.setDescription("Skipping to the next song... try again later!");
        builder.addField("Exact Exception", "||`" + exception.toString() + "`||", true);
        builder.addField("Caused By", "||`" + exception.getCause().toString() + "`||", true);
        builder.addField("Time", "||<t:" + (System.currentTimeMillis()/1000L) + ":R>||", true);
        builder.setFooter("Please notify CyberedCake (@cyberedcake) or <@351410272256262145>");
        builder.setColor(Embeds.ERROR_COLOR);
        builder.setTimestamp(new Date().toInstant());
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public void endQueue(Queue queue) {
        if(!Main.queueManager.checkQueueExists(this.guild)) return;
        queue.getAudioPlayer().stopTrack();

        TextChannel channel = queue.getTextChannel();

        try {
            queue.destroy();
        } catch (Exception exception) {
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("No more songs.");
        builder.setDescription("All songs from the queue have been played and repeat mode is off. The bot is now leaving the voice chat.");
        builder.setColor(new Color(255, 152, 68));
        builder.setTimestamp(new Date().toInstant());
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public Stack<AudioTrack> getQueue() { return this.queue; }

    public Repeating isRepeating() { return this.repeating; }
    public void setRepeating(Repeating repeating) { this.repeating = repeating; }
}
