package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.concurrent.TimeUnit;

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

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(queue.getVoiceChannel())) {
            Embeds.throwError(callback, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        if(!queue.getTrackScheduler().pause()) {
            Embeds.throwError(callback, member.getUser(), "The queue is already playing, use </pause:1084986931986833530> to pause the song", true, null); return;
        }

        queue.getTrackScheduler().pause(false);
        if(showMessage)
            callback.reply(":arrow_forward: You resumed the queue. Use </pause:1084986931986833530> to re-pause it!").queue();
        else
            callback.reply(":arrow_forward: You resumed the queue.").setEphemeral(true).complete().deleteOriginal().queueAfter(3L, TimeUnit.SECONDS);
        removePauseNickname(member.getGuild());

        queue.getTrackScheduler().sendNowPlayingStatus(queue.getAudioPlayer().getPlayingTrack(), TrackScheduler.ToDoWithOld.EDIT);
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

    public static void removePauseNickname(Guild guild) {
        Member selfMember = guild.getSelfMember();
        if(selfMember.getNickname() == null || !selfMember.getNickname().contains(" ⏸")) return;
        selfMember.modifyNickname(selfMember.getNickname().replace(" ⏸", "")).queue();
    }
}
