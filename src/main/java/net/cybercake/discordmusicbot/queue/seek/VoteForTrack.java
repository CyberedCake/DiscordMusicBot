package net.cybercake.discordmusicbot.queue.seek;

import net.cybercake.discordmusicbot.utilities.NumberUtils;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class VoteForTrack {

    public static VoteForTrack empty() {
        return new VoteForTrack(null, null);
    }

    private final SeekType type;
    private final List<QueueSeekUserVote> votes;
    private final SongQueueSeekManager parentManager;

    VoteForTrack(SeekType type, SongQueueSeekManager parentManager) {
        this.type = type;
        this.votes = new ArrayList<>();
        this.parentManager = parentManager;
    }

    public SeekType getSeekType() { return this.type; }
    public List<QueueSeekUserVote> getVotes() { return this.votes; }

    public int getMembersWantingToSeek() { return this.getVotes().size(); }

    public int getMembersWantingToSeekPercentage() {
        return NumberUtils.asRoundedInt(((double)this.getMembersWantingToSeek() / (double)this.parentManager.getMaxNeededToSeek()) * (double)100);
    }

    public boolean successful() {
        return getMembersWantingToSeekPercentage() >= 100;
    }

    public VoteForTrack add(Member member) {
        this.votes.add(new QueueSeekUserVote(member, System.currentTimeMillis(), this.parentManager.musicPlayer.getAudioPlayer().getPlayingTrack(), this.type)); return this;
    }

    public boolean isEmpty() { return this.type == null && this.parentManager == null; }

}
