package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.concurrent.TimeUnit;

public class Pause extends Command {

    public Pause() {
        super("pause", "Pauses any music playing in the voice chat.");
        this.registerButtonInteraction = true;
    }

    public static User lastPauser;

    public void handlePause(IReplyCallback event, boolean showMessage) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to pause the song", true, null); return;
        }

        if(queue.getTrackScheduler().pause()) {
            Embeds.throwError(event, member.getUser(), "The queue is already paused, use </resume:1084986931986833531> to continue the song", true, null); return;
        }

        queue.getTrackScheduler().pause(true);
        if(showMessage)
            event.reply(":pause_button: You paused the queue. Use </resume:1084986931986833531> to unpause it!")
                .setActionRow(Button.success("resume", "Resume the song"))
                .queue();
        else
            event.reply(":pause_button: You paused the queue.").setEphemeral(true).complete().deleteOriginal().queueAfter(3L, TimeUnit.SECONDS);
        addPauseNickname(member.getGuild());
        lastPauser = member.getUser();

        queue.getTrackScheduler().sendNowPlayingStatus(queue.getAudioPlayer().getPlayingTrack(), TrackScheduler.ToDoWithOld.EDIT);

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

    public static void addPauseNickname(Guild guild) {
        Member selfMember = guild.getSelfMember();
        if(selfMember.getEffectiveName().contains("⏸")) return;
        selfMember.modifyNickname(selfMember.getEffectiveName() + " ⏸").queue();
    }
}
