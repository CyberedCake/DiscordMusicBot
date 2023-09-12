package net.cybercake.discordmusicbot.queue;

import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MusicPlayerManager {

    private final Map<Long, MusicPlayer> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public MusicPlayerManager() {
        this.musicManagers = new HashMap<>();

        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, Main.SPOTIFY_CLIENT, Main.SPOTIFY_TOKEN, "US", this.audioPlayerManager));

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

        Log.info("Queue manager has been created!");
    }

    public AudioPlayerManager getAudioPlayerManager() { return audioPlayerManager; }

    public Map<Long, MusicPlayer> getAllQueues() { return this.musicManagers; }

    public boolean checkQueueExists(Guild guild) {
        return this.musicManagers.get(Long.parseLong(guild.getId())) != null;
    }

    public synchronized MusicPlayer getGuildQueue(Guild guild, @Nullable AudioChannelUnion voiceChannel, @Nullable TextChannel textChannel) {
        long guildId = Long.parseLong(guild.getId());
        MusicPlayer musicPlayer = this.musicManagers.get(guildId);

        if(!checkQueueExists(guild)) {
            if(voiceChannel != null && textChannel != null) musicPlayer = createQueue(guild, voiceChannel, textChannel);
            if(voiceChannel == null) throw new IllegalArgumentException("Failed to find a queue for the guild " + guildId + " (" + guild.getName() + ")");
        }

        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(musicPlayer.getAudioPlayer()));

        return musicPlayer;
    }

    public synchronized MusicPlayer getGuildQueue(Guild guild) {
        return getGuildQueue(guild, null, null);
    }

    public synchronized MusicPlayer createQueue(Guild guild, AudioChannelUnion voiceChannel, TextChannel textChannel) {
        Log.info("Created a new queue for guild " + guild.getId() + " (" + guild.getName() + ")" + " in voice channel " + voiceChannel.getId() + " (" + voiceChannel.getName() + ")");
        MusicPlayer musicPlayer = new MusicPlayer(audioPlayerManager, guild, voiceChannel, textChannel);
        musicManagers.put(Long.valueOf(guild.getId()), musicPlayer);
        return musicPlayer;
    }

    protected void removeQueue(MusicPlayer musicPlayer) {
        Log.info("Closing audio connection and deleting queue for guild " + musicPlayer.getGuild().getId() + " (" + musicPlayer.getGuild().getName() + ")...");
        Map.Entry<Long, MusicPlayer> entry = this.musicManagers.entrySet().stream()
                .filter((queueEntry) -> queueEntry.getValue().equals(musicPlayer))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(MusicPlayer.class.getCanonicalName() + " provided in '" + MusicPlayerManager.class.getCanonicalName() + ".removeQueue' does not exist and is not stored by " + MusicPlayerManager.class.getCanonicalName() + "! Found these: " + this.musicManagers.toString()));
        this.musicManagers.remove(entry.getKey());
    }




}
