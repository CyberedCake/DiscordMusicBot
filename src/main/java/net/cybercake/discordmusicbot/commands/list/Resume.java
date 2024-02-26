package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.cybercake.discordmusicbot.utilities.Embeds;
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
        this.requireDjRole = true;
    }

    public void handleResume(IReplyCallback event) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(event instanceof ButtonInteractionEvent
                && !musicPlayer.getTrackScheduler().pause()
        ) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        if(!musicPlayer.getTrackScheduler().pause()) {
            Embeds.throwError(event, member.getUser(), "The queue is already playing, use " + Command.getCommandClass(Pause.class).getJdaCommand().getAsMention() + " to pause the song", true, null); return;
        }

        musicPlayer.getTrackScheduler().pause(false);
        event.reply(":arrow_forward: " + member.getEffectiveName() + " resumed the queue. Use " + Command.getCommandClass(Pause.class).getJdaCommand().getAsMention() + " to re-pause it!").queue();
            // event.reply(":arrow_forward: You resumed the queue.").setEphemeral(true).complete().deleteOriginal().queueAfter(3L, TimeUnit.SECONDS);
        removePauseNickname(member.getGuild());

        musicPlayer.getTrackScheduler().sendSongPlayingStatus(musicPlayer.getAudioPlayer().getPlayingTrack(), TrackScheduler.ToDoWithOld.EDIT);
    }

                             @Override
    public void command(SlashCommandInteractionEvent event) {
        handleResume(event);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("resume")) return;
        if(event.isAcknowledged()) return;
        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild());
        if(buttonId.contains("pauseresume") && (musicPlayer != null && !musicPlayer.getTrackScheduler().pause())) return;
        handleResume(event);
    }

    public static void removePauseNickname(Guild guild) {
        Member selfMember = guild.getSelfMember();
        if(selfMember.getNickname() == null || !selfMember.getNickname().contains("⏸")) return;
        selfMember.modifyNickname(selfMember.getNickname().replace("⏸", "")).queue();
    }
}
