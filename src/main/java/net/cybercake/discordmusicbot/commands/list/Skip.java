package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class Skip extends Command {

    public Skip() {
        super(
                "skip", "Skips the current track to the next track via a vote.", "s"
        );
    }

    public static void handleSkip(Member member, IReplyCallback callback, String identifier) {
        Queue queue = PresetExceptions.trackIsNotPlaying(callback, member, true);
        if(queue == null) return;

        if(!queue.getAudioPlayer().getPlayingTrack().getIdentifier().equalsIgnoreCase(identifier)) {
            Embeds.throwError(callback, member.getUser(), "That song is no longer playing.", true, null); return;
        }

        if(queue.hasMemberSkipVoted(member)) {
            Embeds.throwError(callback, member.getUser(), "You already voted to skip this song.", true, null); return;
        }

        queue.addMemberToSkipVote(member);

        callback.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        TextChannel channel = (TextChannel) callback.getMessageChannel();
        channel.sendMessage(member.getAsMention() + " has requested to skip `" + queue.getAudioPlayer().getPlayingTrack().getInfo().title + "`. **[" + queue.getMembersWantingSkip() + "/" + queue.getMaxNeededToSkip() + " - " + (queue.getMembersWantingSkipPercent()) + "%]**" + (queue.getMembersWantingSkipPercent() >= 100 ? "\n\n*Skipping this song...*" : "")).queue();
        queue.checkSkipProportion();
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Skip.handleSkip(event.getMember(), event, Main.queueManager.getGuildQueue(event.getGuild()).getAudioPlayer().getPlayingTrack().getIdentifier());
    }
}
