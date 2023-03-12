package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

public class TrackScheduler extends AudioEventAdapter {

    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final Stack<AudioTrack> queue;

    public enum Repeating { FALSE, REPEATING_SONG, REPEATING_ALL }
    private Repeating repeating;

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

    public void nextTrack() {
        if(queue.size() < 1) {
            endQueue(Main.queueManager.getGuildQueue(guild)); return;
        }
        AudioTrack nextTrack = queue.firstElement();
        queue.remove(nextTrack);

        try {
            audioPlayer.startTrack(nextTrack, false);
        } catch (IllegalStateException illegalStateException) {
            Log.error("Failed in starting next track", illegalStateException);
            if(!illegalStateException.getMessage().contains("Cannot play the same instance")) return;
            endQueue(Main.queueManager.getGuildQueue(this.guild));
        }
    }

    public void shuffle() {
        if(queue.size() < 1) throw new IllegalStateException("Cannot shuffle a queue that is less than one item.");

        Collections.shuffle(queue, new Random(System.currentTimeMillis()));
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        try {
            Thread sendNowPlayingMessage = new Thread(() -> {
                try {
                    Thread.sleep(600L); // delay because information that is set after this method finishes is required inside the embed
                    if(!Main.queueManager.checkQueueExists(this.guild)) return;
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
        if(!Main.queueManager.checkQueueExists(this.guild)) return;
        Queue queueMain = Main.queueManager.getGuildQueue(this.guild);
        queueMain.getSkipSongManager().clearSkipVoteQueue();
        if(queue.size() < 1) {
            endQueue(queueMain); return;
        }
        if(endReason.mayStartNext) {
            switch(this.repeating) {
                case REPEATING_SONG -> player.startTrack(track.makeClone(), false);
                case REPEATING_ALL, FALSE -> nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Log.error("An error occurred while playing a track", exception);
        if(this.message != null) { // delete previous message if it exists
            this.message.getFirstItem().deleteMessageById(this.message.getSecondItem()).queue();
            this.message = null;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("An error occurred playing `" + track.getInfo().title + "`");
        builder.setDescription("Skipping to the next song... try again later!");
        builder.addField("Exact Exception", "||`" + exception.toString() + "`||", true);
        builder.addField("Exception Type", "||`" + exception.getClass().getCanonicalName() + "`||", true);
        builder.addField("Time", "||`<t:" + (System.currentTimeMillis()/1000L) + ":R>`||", true);
        builder.setColor(Embeds.ERROR_COLOR);
        builder.setTimestamp(new Date().toInstant());
        Main.queueManager.getGuildQueue(guild).getTextChannel().sendMessageEmbeds(builder.build()).queue();
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
