package net.cybercake.discordmusicbot.commands.list.admin;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

public class ForceSkip extends Command {

    public ForceSkip() {
        super(
                "forceskip", "Forces the current song to be skipped."
        );
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER, Permission.ADMINISTRATOR, Permission.VOICE_MOVE_OTHERS
        );
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        queue.getTrackScheduler().nextTrack();
        event.reply("You skipped the current track, advancing to the next one...").queue();
    }
}

