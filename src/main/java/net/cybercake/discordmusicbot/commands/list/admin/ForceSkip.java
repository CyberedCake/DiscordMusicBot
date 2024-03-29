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

public class ForceSkip extends Command {

    public ForceSkip() {
        super(
                "forceskip", "Forces the current song to be skipped."
        );
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.VOICE_MOVE_OTHERS
        );
        this.optionData = new OptionData[]{
                new OptionData(OptionType.INTEGER, "tracks", "How many tracks to skip. The default is 0.", false)
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

        musicPlayer.getTrackScheduler().nextTrack(tracks == null ? 0 : tracks.getAsInt());
        event.reply("You skipped the current track, " + (tracks == null ? "advancing to the next one" : "skipping to track at position `" + tracks.getAsInt() + "`") + "...").queue();
    }
}

