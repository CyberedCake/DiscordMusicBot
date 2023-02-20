package net.cybercake.discordmusicbot.queue;

import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class QueueManager {

    private final Map<Long, Queue> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public QueueManager() {
        this.musicManagers = new HashMap<>();

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, Main.SPOTIFY_CLIENT, Main.SPOTIFY_TOKEN, "US", this.audioPlayerManager));

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        Log.info("Queue manager has been created!");
    }

    public AudioPlayerManager getAudioPlayerManager() { return audioPlayerManager; }

    public Map<Long, Queue> getAllQueues() { return this.musicManagers; }

    public boolean checkQueueExists(Guild guild) {
        return this.musicManagers.get(Long.parseLong(guild.getId())) != null;
    }

    public synchronized Queue getGuildQueue(Guild guild, @Nullable VoiceChannel voiceChannel, @Nullable TextChannel textChannel) {
        long guildId = Long.parseLong(guild.getId());
        Queue queue = this.musicManagers.get(guildId);

        if(!checkQueueExists(guild)) {
            if(voiceChannel != null && textChannel != null) queue = createQueue(guild, voiceChannel, textChannel);
            if(voiceChannel == null) throw new IllegalArgumentException("Failed to find a queue for the guild " + guildId + " (" + guild.getName() + ")");
        }

        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(queue.getAudioPlayer()));

        return queue;
    }

    public synchronized Queue getGuildQueue(Guild guild) {
        return getGuildQueue(guild, null, null);
    }

    public synchronized Queue createQueue(Guild guild, VoiceChannel voiceChannel, TextChannel textChannel) {
        Log.info("Created a new queue for guild " + guild.getId() + " (" + guild.getName() + ")" + " in voice channel " + voiceChannel.getId() + " (" + voiceChannel.getName() + ")");
        Queue queue = new Queue(audioPlayerManager, guild, voiceChannel, textChannel);
        musicManagers.put(Long.valueOf(guild.getId()), queue);
        return queue;
    }

    protected void removeQueue(Queue queue) {
        Log.info("Closing audio connection and deleting queue for guild " + queue.getGuild().getId() + " (" + queue.getGuild().getName() + ")...");
        Map.Entry<Long, Queue> entry = this.musicManagers.entrySet().stream()
                .filter((queueEntry) -> queueEntry.getValue().equals(queue))
                .findFirst()
                .orElseThrow(() -> { throw new IllegalArgumentException(Queue.class.getCanonicalName() + " provided in '" + QueueManager.class.getCanonicalName() + ".removeQueue' does not exist and is not stored by " + QueueManager.class.getCanonicalName() + "! Found these: " + this.musicManagers.toString()); });
        this.musicManagers.remove(entry.getKey());
    }




}
