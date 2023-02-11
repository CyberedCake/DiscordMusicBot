package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class QueueManager {

    private final Map<Long, Queue> musicManagers;
    private AudioPlayerManager audioPlayerManager;

    public QueueManager() {
        this.musicManagers = new HashMap<>();

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        Log.info("Queue manager has been created!");
    }

    public Queue createQueue(Guild guild, VoiceChannel voiceChannel) {
        Log.info("Created a new queue for guild " + guild.getId() + " (" + guild.getName() + ")" + " in voice channel " + voiceChannel.getId() + " (" + voiceChannel.getName() + ")");
        Queue queue = new Queue(audioPlayerManager, guild, voiceChannel);
        musicManagers.put(Long.parseLong(guild.getId()), queue);
        return queue;
    }

    public AudioPlayerManager getAudioPlayerManager() { return audioPlayerManager; }

    public Queue getQueueFor(Guild guild) { return getQueueFor(Long.parseLong(guild.getId())); }
    public Queue getQueueFor(long guildId) { return this.musicManagers.get(guildId); }

    private synchronized Queue getGuildAudioPlayer(Guild guild, VoiceChannel voiceChannel) {
        long guildId = Long.parseLong(guild.getId());
        Queue queue = this.musicManagers.get(guildId);

        if(queue == null) {
            queue = Main.queueManager.createQueue(guild, voiceChannel);
            musicManagers.put(Long.valueOf(guild.getId()), queue);
        }

        guild.getAudioManager().setSendingHandler(queue.getSendHandler());

        return queue;
    }




}
