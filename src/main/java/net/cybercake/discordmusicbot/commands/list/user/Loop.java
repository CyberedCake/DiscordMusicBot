package net.cybercake.discordmusicbot.commands.list.user;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Reply;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;

public class Loop extends Command {

    public Loop() {
        super("loop", "Loops the current song or the entire queue.");
        this.requireDjRole = true;
        this.aliases = new String[]{"repeat"};
        this.subCommands = new SubcommandData[]{
                new SubcommandData("song", "Loops the current song."),
                new SubcommandData("queue", "Loops the entire queue.")
        };
        this.registerButtonInteraction = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(subCommands == null) {
            Embeds.executeEmbed(event, Embeds.getTechnicalErrorEmbed(event.getUser(), "no subCommands for " + Loop.class.getCanonicalName() + " found: []"), true); return;
        }

        String subCommand = event.getSubcommandName();
        if(subCommand == null) {
            Embeds.throwError(event, event.getUser(), "Subcommand is null, options: " + String.join(", ", Arrays.stream(this.subCommands).map(SubcommandData::getName).toList()), true, null);
            return;
        }

        if(subCommand.equalsIgnoreCase("song"))
            handleLoop(event, musicPlayer, TrackScheduler.Repeating.REPEATING_SONG);
        else if(subCommand.equalsIgnoreCase("queue"))
            handleLoop(event, musicPlayer, TrackScheduler.Repeating.REPEATING_ALL);
        else
            Embeds.throwError(event, event.getUser(), "Invalid sub-command: " + subCommand, true, null);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.startsWith("loop")) return;

        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(buttonId.contains("song")) handleLoop(event, musicPlayer, TrackScheduler.Repeating.REPEATING_SONG);
        else if(buttonId.contains("queue")) handleLoop(event, musicPlayer, TrackScheduler.Repeating.REPEATING_ALL);
    }

    private void handleLoop(IReplyCallback event, MusicPlayer musicPlayer, TrackScheduler.Repeating repeatingType) {
        TrackScheduler scheduler = musicPlayer.getTrackScheduler();

        TrackScheduler.Repeating old = scheduler.repeating();
        TrackScheduler.Repeating repeating = scheduler.repeating() == repeatingType
                ? TrackScheduler.Repeating.FALSE
                : repeatingType;
        scheduler.repeating(repeating);

        if(repeating == repeatingType)
            Reply.standardEmbed(event, Colors.OTF_SETTINGS,
                    event.getUser().getAsMention() + " enabled looping " + repeating.text() + " " + Emoji.fromUnicode("✅").getFormatted() + " " + repeating.emoji());
        else
            Reply.standardEmbed(event, Colors.OTF_SETTINGS,
                    event.getUser().getAsMention() + " disabled looping " + old.text() + " " + Emoji.fromUnicode("❌").getFormatted() + " " + old.emoji());
    }
}
