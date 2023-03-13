package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Pause extends Command {

    public Pause() {
        super("pause", "Pauses any music playing in the voice chat.");
        this.registerButtonInteraction = true;
    }

    public void handlePause(IReplyCallback event, boolean showMessage) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().asVoiceChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to pause the song", true, null); return;
        }

        if(queue.getTrackScheduler().pause()) {
            Embeds.throwError(event, member.getUser(), "The queue is already paused, use </resume:code> to continue the song", true, null); return;
        }

        queue.getTrackScheduler().pause(true);
        if(showMessage)
            event.reply(":pause_button: You paused the queue. Use </resume:code> to unpause it!")
                .setActionRow(Button.success("resume", "Resume the song"))
                .queue();
        else
            event.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
        Member selfMember = member.getGuild().getSelfMember();
        selfMember.modifyNickname(selfMember.getEffectiveName() + " ⏸").queue();

        queue.getTrackScheduler().sendNowPlayingStatus(queue.getAudioPlayer().getPlayingTrack(), true);

    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        handlePause(event, true);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("pause")) return;
        handlePause(event, !buttonId.contains("-nomsg"));
    }
}
