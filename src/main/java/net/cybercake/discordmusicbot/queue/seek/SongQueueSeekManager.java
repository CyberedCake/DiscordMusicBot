package net.cybercake.discordmusicbot.queue.seek;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.MapUtils;
import net.cybercake.discordmusicbot.utilities.NumberUtils;
import net.cybercake.discordmusicbot.utilities.Pair;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;

import java.util.*;

public class SongQueueSeekManager {

    private static Map<MusicPlayer, SongQueueSeekManager> managers = new HashMap<>();
    public static SongQueueSeekManager as(MusicPlayer player) {
        Pair<SongQueueSeekManager, Map<MusicPlayer, SongQueueSeekManager>> pair = MapUtils.getOrAddDefault(managers, player, new SongQueueSeekManager(player));
        if (pair.isSecondItemSet())
            managers = pair.getSecondItem();
        return pair.getFirstItem();
    }





    private final List<VoteForTrack> activeVotes;
    final MusicPlayer musicPlayer;

    private SongQueueSeekManager(MusicPlayer musicPlayer) {
        this.activeVotes = new ArrayList<>();
        this.musicPlayer = musicPlayer;
    }

    public void clear() {
        this.activeVotes.clear();
    }

    public boolean hasMemberVoted(Member member, SeekType type) {
        return activeVotes.stream().filter(vote -> vote.getSeekType() == type).findFirst().orElse(VoteForTrack.empty())
                .getVotes().stream().map(user -> user.member().getIdLong() == member.getIdLong()).findAny().orElse(false);
    }

    public VoteForTrack getVotesForTrack(SeekType type) {
        return activeVotes.stream().filter(track -> track.getSeekType() == type).findFirst().orElse(null);
    }

    private VoteForTrack getOrCreate(SeekType type) {
        VoteForTrack vote = getVotesForTrack(type);
        if (vote == null) vote = new VoteForTrack(type, this);
        return vote;
    }

    public void addUser(Member member, SeekType type) {
        if (hasMemberVoted(member, type))
            throw new IllegalArgumentException("That member, " + member.getIdLong() + " (" + member.getEffectiveName() + "), has already casted a vote for type " + type.name());
        activeVotes.add(getOrCreate(type).add(member));
    }

    public List<Member> getUsersWhoCanSeekQueue() {
        return this.musicPlayer.getVoiceChannel().getMembers()
                .stream()
                .filter(member -> !member.getUser().isBot())
                .filter(member -> this.musicPlayer.getVoiceChannel().getType() != ChannelType.STAGE
                        || (this.musicPlayer.getVoiceChannel().asStageChannel().getStageInstance() != null
                        && Objects.requireNonNull(this.musicPlayer.getVoiceChannel().asStageChannel().getStageInstance()).getSpeakers().contains(member))
                )
                .toList();
    }

    public int getMaxNeededToSeek() {
        return NumberUtils.asRoundedInt(this.getUsersWhoCanSeekQueue().size() * Main.QUEUE_SEEK_PERCENTAGE);
    }

    public void checkSeekProportion(SeekType type) {
        if (!getOrCreate(type).successful()) return;

        switch (type) {
            case NEXT -> this.musicPlayer.getTrackScheduler().nextTrack();
            case PREVIOUS -> this.musicPlayer.getTrackScheduler().previousTrack();
        }
    }
}
