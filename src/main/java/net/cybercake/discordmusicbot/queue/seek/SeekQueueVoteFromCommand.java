package net.cybercake.discordmusicbot.queue.seek;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.cybercake.discordmusicbot.utilities.YouTubeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class SeekQueueVoteFromCommand {

    public static void handle(SeekType type, MusicPlayer musicPlayer, Member member, IReplyCallback callback, String identifier) {
        if(!musicPlayer.getAudioPlayer().getPlayingTrack().getIdentifier().equalsIgnoreCase(identifier)) {
            Embeds.throwError(callback, member.getUser(), "That song is no longer playing.", true, null); return;
        }

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(callback, member.getUser(), "You must be in the voice chat to do this.", true, null); return;
        }

        Queue queue = musicPlayer.getTrackScheduler().getQueue();
        switch (type) {
            case NEXT -> {
                if (queue.getCurrentIndex() >= queue.getLiteralQueue().size()) {
                    Embeds.throwError(callback, member.getUser(), "You can't skip the last song in the playlist.", true, null); return;
                }
            }
            case PREVIOUS -> {
                if (queue.getCurrentIndex() <= 1) {
                    Embeds.throwError(callback, member.getUser(), "You can't go the previous song when the current song is the first in the queue.", true, null); return;
                }
            }
        }

        SongQueueSeekManager seekManager = musicPlayer.getSeekManager();
        if(seekManager.hasMemberVoted(member, type)) {
            Embeds.throwError(callback, member.getUser(), "You already voted to " + type.semantics + ".", true, null); return;
        }

        seekManager.addUser(member, type);

        callback.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        VoteForTrack seekVote = seekManager.getVotesForTrack(type);
        TextChannel channel = (TextChannel) callback.getMessageChannel();
        if (seekManager.getUsersWhoCanSeekQueue().size() > 1) {
            // channel.sendMessage(member.getAsMention() + " has requested to skip `" + musicPlayer.getAudioPlayer().getPlayingTrack().getInfo().title + "`. **[" + seekVote.getMembersWantingToSeek() + "/" + seekManager.getMaxNeededToSeek() + " - " + (seekVote.getMembersWantingToSeekPercentage()) + "%]**" + (seekVote.getMembersWantingToSeekPercentage() >= 100 ? "\n\n*Skipping this song...*" : "")).queue();
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(type.title + ": " + seekVote.getMembersWantingToSeek() + "/" + seekManager.getMaxNeededToSeek() + " - " + seekVote.getMembersWantingToSeekPercentage() + "%");
            AudioTrack track = musicPlayer.getAudioPlayer().getPlayingTrack();
            embed.setDescription(member.getAsMention() + " " +
                    type.subtitle.replace("<previous-song>", "__" + previous(queue) + "__") + " [" + track.getInfo().title + "](" + TrackUtils.getUrlOf(track.getInfo()) + ") by " + track.getInfo().author + (seekVote.getMembersWantingToSeekPercentage() >= 100 ? "\n" +
                    "*The vote has passed, " + type.execution + ".*" : "")
            );
            embed.setThumbnail(YouTubeUtils.extractImage(track.getInfo()));
            embed.setColor(type.getColor().get());
            channel.sendMessageEmbeds(embed.build()).queue();
        }
        seekManager.checkSeekProportion(type);
    }

    private static String previous(Queue queue) {
        try {
            return queue.getLiteralQueue().get(queue.getCurrentIndex() - 2).getInfo().title;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return "null";
        }
    }

}
