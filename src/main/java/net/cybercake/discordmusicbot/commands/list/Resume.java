package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Resume extends Command {

    public Resume() {
        super("resume", "Resumes the bot to continue playing.");
        this.registerButtonInteraction = true;
    }

    public void handleResume(IReplyCallback callback, boolean showMessage) {
        if(PresetExceptions.memberNull(callback)) return;
        Member member = callback.getMember();
        assert member != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(callback, callback.getMember(), true);
        if(queue == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().asVoiceChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(callback, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        if(!queue.getTrackScheduler().pause()) {
            Embeds.throwError(callback, member.getUser(), "The queue is already playing, use </pause:code> to pause the song", true, null); return;
        }

        queue.getTrackScheduler().pause(false);
        if(showMessage)
            callback.reply(":arrow_forward: You resumed the queue. Use </pause:code> to re-pause it!").queue();
        else
            callback.deferReply().setEphemeral(true).complete().deleteOriginal().queue();
        Member selfMember = member.getGuild().getSelfMember();
        if(selfMember.getNickname() != null && selfMember.getNickname().contains(" ⏸"))
            selfMember.modifyNickname(selfMember.getNickname().replace(" ⏸", "")).queue();

        queue.getTrackScheduler().sendNowPlayingStatus(queue.getAudioPlayer().getPlayingTrack(), true);
    }

                             @Override
    public void command(SlashCommandInteractionEvent event) {
        handleResume(event, true);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("resume")) return;
        handleResume(event, !buttonId.contains("-nomsg"));
    }
}
