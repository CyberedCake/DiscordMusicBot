package net.cybercake.discordmusicbot.commands.list.admin;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ForcePrevious extends Command {

    public ForcePrevious() {
        super(
                "forceprevious", "Forces the queue to go back a song."
        );
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.VOICE_MOVE_OTHERS
        );
        this.optionData = new OptionData[]{
                new OptionData(OptionType.INTEGER, "tracks", "How many tracks to go back. The default is 1.", false)
        };
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        OptionMapping tracks = event.getOption("tracks");

        musicPlayer.getTrackScheduler().previousTrack(tracks == null ? 1 : tracks.getAsInt());
        event.reply("You changed the queue status " + (tracks == null ? "and went back a song" : "going back `" + tracks.getAsInt() + "`" + " songs") + "...").queue();
    }
}