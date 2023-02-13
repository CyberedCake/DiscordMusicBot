package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Skip extends Command {

    public Skip() {
        super(
                "skip", "Skips the current track to the next track via a vote."
        );
        this.aliases = new String[]{"s"};
    }

    public static void handleSkip(Queue queue, Member member, IReplyCallback callback, String identifier) {
        if(!queue.getAudioPlayer().getPlayingTrack().getIdentifier().equalsIgnoreCase(identifier)) {
            Embeds.throwError(callback, member.getUser(), "That song is no longer playing.", true, null); return;
        }

        if(queue.getSkipSongManager().hasMemberSkipVoted(member)) {
            Embeds.throwError(callback, member.getUser(), "You already voted to skip this song.", true, null); return;
        }

        queue.getSkipSongManager().addMemberToSkipVote(member);

        callback.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        TextChannel channel = (TextChannel) callback.getMessageChannel();
        channel.sendMessage(member.getAsMention() + " has requested to skip `" + queue.getAudioPlayer().getPlayingTrack().getInfo().title + "`. **[" + queue.getSkipSongManager().getMembersWantingSkip() + "/" + queue.getSkipSongManager().getMaxNeededToSkip() + " - " + (queue.getSkipSongManager().getMembersWantingSkipPercent()) + "%]**" + (queue.getSkipSongManager().getMembersWantingSkipPercent() >= 100 ? "\n\n*Skipping this song...*" : "")).queue();
        queue.getSkipSongManager().checkSkipProportion();
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;
        Skip.handleSkip(queue, event.getMember(), event, Main.queueManager.getGuildQueue(event.getGuild()).getAudioPlayer().getPlayingTrack().getIdentifier());
    }
}