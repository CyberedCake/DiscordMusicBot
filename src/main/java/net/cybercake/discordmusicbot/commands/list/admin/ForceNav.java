package net.cybercake.discordmusicbot.commands.list.admin;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ForceNav extends Command {

    public ForceNav() {
        super("forcenav", "Forcibly navigates the queue to a certain track position.");
        this.requireDjRole = true;
        this.permission = DefaultMemberPermissions.enabledFor( // need these permissions to execute
                Permission.VOICE_MOVE_OTHERS
        );
        this.optionData = new OptionData[]{
                new OptionData(OptionType.INTEGER, "to-position", "Skips to this position in the queue.", true)
        };
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        OptionMapping toPosition = event.getOption("to-position");
        if(toPosition == null){
            Embeds.executeEmbed(event, Embeds.getTechnicalErrorEmbed(event.getUser(), "toPosition OptionMapping is null, yet is required"), true); return;
        }

        int position = toPosition.getAsInt() - 1;

        musicPlayer.getTrackScheduler().getQueue().toIndex(position);
        musicPlayer.getTrackScheduler().nextTrack();
        event.reply("You forcibly navigated to the song at position `" + (position + 1) + "` in the queue: **" + musicPlayer.getTrackScheduler().getQueue().getLiteralQueue().get(position).getInfo().title + "**").queue();
    }
}
