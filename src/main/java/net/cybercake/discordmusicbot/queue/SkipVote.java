package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.dv8tion.jda.api.entities.Member;

public record SkipVote(Member member, long timestamp, AudioTrack audioTrack) {

    public boolean isExpired() {
        Queue queue = Main.queueManager.getGuildQueue(member.getGuild());
        return (queue == null || !queue.getAudioPlayer().getPlayingTrack().equals(audioTrack));
    }

}
