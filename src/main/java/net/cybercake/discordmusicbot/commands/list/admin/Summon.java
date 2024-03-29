package net.cybercake.discordmusicbot.commands.list.admin;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class Summon extends Command {

    public Summon() {
        super("summon", "Summons the music bot to your channel.");
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.VOICE_MOVE_OTHERS
        );
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;
        Member member = event.getMember();

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null) { // they are not in a voice chat
            Embeds.throwError(event, member.getUser(), "You must be in a voice channel to continue.", true, null); return;
        }

        musicPlayer.setVoiceChannel(member.getVoiceState().getChannel(), event);
    }
}
