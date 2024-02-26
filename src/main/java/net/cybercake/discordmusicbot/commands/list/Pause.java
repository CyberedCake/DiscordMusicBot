package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.cybercake.discordmusicbot.utilities.Embeds;
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
        this.requireDjRole = true;
    }

    public static User lastPauser;

    public void handlePause(IReplyCallback event) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to pause the song", true, null); return;
        }

        if(musicPlayer.getTrackScheduler().pause()) {
            Embeds.throwError(event, member.getUser(), "The queue is already paused, use " + Command.getCommandClass(Resume.class).getJdaCommand().getAsMention() + " to continue the song", true, null); return;
        }

        musicPlayer.getTrackScheduler().pause(true);
        event.reply(":pause_button: " + member.getEffectiveName() + " paused the queue. Use " + Command.getCommandClass(Resume.class).getJdaCommand().getAsMention() + " to unpause it!").queue();
            // event.reply(":pause_button: You paused the queue.").setEphemeral(true).complete().deleteOriginal().queueAfter(3L, TimeUnit.SECONDS);
        addPauseNickname(member.getGuild());
        lastPauser = member.getUser();

        musicPlayer.getTrackScheduler().sendSongPlayingStatus(musicPlayer.getAudioPlayer().getPlayingTrack(), TrackScheduler.ToDoWithOld.EDIT);

    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        handlePause(event);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("pause")) return;
        if(event.isAcknowledged()) return;
        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild());
        if(buttonId.contains("pauseresume") && (musicPlayer != null && musicPlayer.getTrackScheduler().pause())) return;
        handlePause(event);
    }

    public static void addPauseNickname(Guild guild) {
        Member selfMember = guild.getSelfMember();
        if(selfMember.getEffectiveName().contains("⏸")) return;
        selfMember.modifyNickname(selfMember.getEffectiveName() + " ⏸").queue();
    }
}
