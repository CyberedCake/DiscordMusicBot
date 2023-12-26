package net.cybercake.discordmusicbot.queue.seek;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.dv8tion.jda.api.entities.Member;

public record QueueSeekUserVote(Member member, long timestamp, AudioTrack audioTrack, SeekType type) {

    public boolean isExpired() {
        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(member.getGuild());
        return (musicPlayer == null || !musicPlayer.getAudioPlayer().getPlayingTrack().equals(audioTrack));
    }

}
