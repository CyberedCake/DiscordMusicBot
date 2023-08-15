package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
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
        super("loop", "Loops the song or playlist. **PLAYLIST LOOP COMING SOON.**");
        this.requireDjRole = true;
        this.aliases = new String[]{"repeat"};
        this.subCommands = new SubcommandData[]{
                new SubcommandData("song", "Loops the current song."),
                new SubcommandData("queue", "Loops the queue. **COMING SOON!**")
        };
        this.registerButtonInteraction = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(subCommands == null) {
            Embeds.executeEmbed(event, Embeds.getTechnicalErrorEmbed(event.getUser(), "no subCommands for " + Loop.class.getCanonicalName() + " found: []"), true); return;
        }

        String subCommand = event.getSubcommandName();
        if(subCommand == null) {
            Embeds.throwError(event, event.getUser(), "Subcommand is null, options: " + String.join(", ", Arrays.stream(this.subCommands).map(SubcommandData::getName).toList()), true, null);
            return;
        }

        if(subCommand.equalsIgnoreCase("song"))
            handleLoopSong(event, queue);
        else if(subCommand.equalsIgnoreCase("queue"))
            handleLoopQueue(event, queue);
        else
            Embeds.throwError(event, event.getUser(), "Invalid sub-command: " + subCommand, true, null);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.startsWith("loop")) return;

        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(buttonId.contains("song")) handleLoopSong(event, queue);
        else if(buttonId.contains("queue")) handleLoopQueue(event, queue);
    }

    private void handleLoopSong(IReplyCallback event, Queue queue) {
        TrackScheduler scheduler = queue.getTrackScheduler();
        TrackScheduler.Repeating repeatingNew = scheduler.repeating() == TrackScheduler.Repeating.REPEATING_SONG
                ? TrackScheduler.Repeating.FALSE
                : TrackScheduler.Repeating.REPEATING_SONG;
        scheduler.repeating(repeatingNew);

        if(repeatingNew == TrackScheduler.Repeating.REPEATING_SONG)
            Reply.message(event, Emoji.fromUnicode("✅").getFormatted() + Emoji.fromUnicode("\uDD02").getFormatted() +
                    " Loop *for this song* is now **enabled**", true);
        else
            Reply.message(event, Emoji.fromUnicode("❌").getFormatted() + Emoji.fromUnicode("\uDD02").getFormatted() +
                    " Loop *for this song* is now **disabled**", true);
    }

    private void handleLoopQueue(IReplyCallback event, Queue queue) {
        Embeds.throwError(event, event.getUser(), "This feature is **coming soon**, try again later!", true, null);
    }
}
