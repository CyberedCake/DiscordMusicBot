package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.dv8tion.jda.api.entities.Member;

public record SkipVote(Member member, long timestamp, AudioTrack audioTrack) {

    public boolean isExpired() {
        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(member.getGuild());
        return (musicPlayer == null || !musicPlayer.getAudioPlayer().getPlayingTrack().equals(audioTrack));
    }

}
