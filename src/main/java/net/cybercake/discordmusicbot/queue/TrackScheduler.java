package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.list.Resume;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.utilities.Embeds;
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

    final Guild guild;
    final AudioPlayer audioPlayer;
    final Queue queue;

    public enum Repeating {
        FALSE("❌ Loop Disabled"),
        REPEATING_SONG("\uD83D\uDD02 Song Looping"),
        REPEATING_ALL("\uD83D\uDD01 Queue Looping");

        private final String userFriendly;
        Repeating(String userFriendly){  this.userFriendly = userFriendly; }
        public String userFriendlyString() { return this.userFriendly; }
    }
    private Repeating repeating;

    public int TRACK_EXCEPTION_MAXIMUM_REPEATS = 4;
    private String trackExceptionId;
    private int trackExceptionRepeats;

    private @Nullable Pair<TextChannel, Long> message;

    public TrackScheduler(Guild guild, AudioPlayer audioPlayer) {
        this.guild = guild;
        this.audioPlayer = audioPlayer;
        this.queue = new Queue(this);
        this.repeating = Repeating.FALSE;

        this.message = null;
    }

    public Queue getQueue() { return this.queue; }


    public void pause(boolean pause) { this.audioPlayer.setPaused(pause); }
    public boolean pause() { return this.audioPlayer.isPaused(); }

    public void nextTrack() {
        nextTrack(0);
    }

    /**
     * @param skipAmount the amount of songs to skip (default is 0)
     */
    public void nextTrack(int skipAmount) {
        if(this.queue.isEmpty()) {
            endQueue(Main.musicPlayerManager.getGuildMusicPlayer(guild)); return;
        }

        if(skipAmount > 0)
            this.queue.toIndex(this.queue.getCurrentIndex() + skipAmount);

        this.postQueueSeek();
    }

    public void previousTrack(int previousAmount) {
        this.queue.toIndex(this.queue.getCurrentIndex() - (previousAmount + 1));
        this.postQueueSeek();
    }

    public void previousTrack() {
        previousTrack(1);
    }

    private void postQueueSeek() {
        try {
            this.queue.playNextTrack(true);
            this.trackExceptionRepeats = 0;
        } catch (IllegalStateException illegalStateException) {
            Log.error("Failed in starting next track after queue seek", illegalStateException);
            if (!illegalStateException.getMessage().contains("Cannot play the same instance")) return;
            endQueue(Main.musicPlayerManager.getGuildMusicPlayer(this.guild));
        }
    }

    public void shuffle() {
        if(this.queue.isEmpty()) throw new IllegalStateException("Cannot shuffle a queue that is less than one item.");

        this.queue.randomizeOrder();
    }

    public enum ToDoWithOld {
        NOTHING, DELETE, EDIT
    }

    @SuppressWarnings({"all"})
    public void sendSongPlayingStatus(AudioTrack track, ToDoWithOld toDoWithOld) {
        try {
            if(toDoWithOld == ToDoWithOld.DELETE)
                deleteOldSongPlayingStatus();
            Thread sendNowPlayingMessage = new Thread(() -> {
                try {
                    Thread.sleep(600L); // delay because information that is set after this method finishes is required inside the embed
                    if(!Main.musicPlayerManager.checkMusicPlayerExists(this.guild)) return;
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

    public void deleteOldSongPlayingStatus() {
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
        sendSongPlayingStatus(track, ToDoWithOld.NOTHING);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason == AudioTrackEndReason.LOAD_FAILED && this.trackExceptionRepeats < TRACK_EXCEPTION_MAXIMUM_REPEATS)
            return;
        Resume.removePauseNickname(this.guild);
        deleteOldSongPlayingStatus();
        if(!Main.musicPlayerManager.checkMusicPlayerExists(this.guild)) return;
        MusicPlayer musicPlayerMain = Main.musicPlayerManager.getGuildMusicPlayer(this.guild);
        musicPlayerMain.getSeekManager().clear();
        if(queue.isEmpty()) {
            endQueue(musicPlayerMain); return;
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
        TextChannel channel = Main.musicPlayerManager.getGuildMusicPlayer(guild).getTextChannel();
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
        builder.setColor(Colors.ERROR.get());
        builder.setTimestamp(new Date().toInstant());
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public void endQueue(MusicPlayer musicPlayer) {
        if(!Main.musicPlayerManager.checkMusicPlayerExists(this.guild)) return;
        musicPlayer.getAudioPlayer().stopTrack();

        TextChannel channel = musicPlayer.getTextChannel();

        try {
            musicPlayer.destroy();
        } catch (Exception exception) {
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("No more songs.");
        builder.setDescription("All songs from the queue have been played and repeat mode is off. The bot is now leaving the voice chat.");
        builder.setColor(Colors.DISCONNECTED.get());
        builder.setTimestamp(new Date().toInstant());
        channel.sendMessageEmbeds(builder.build()).queue();
    }

    public AudioTrack remove(int position) {
        return this.queue.remove(position);
    }

    public Repeating repeating() { return this.repeating; }
    public void repeating(Repeating repeating) {
        this.repeating = repeating;
        sendSongPlayingStatus(audioPlayer.getPlayingTrack(), ToDoWithOld.EDIT);
    }

    public void destroy() {
        this.queue.destroy();
    }
}
