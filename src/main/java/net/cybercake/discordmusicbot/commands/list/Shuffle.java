package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Shuffle extends Command {

    public Shuffle() {
        super("shuffle", "Mixes up the current playlist into a random order.");
        this.aliases = new String[]{"randomize"};
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        if(PresetExceptions.isNotInVoiceChat(event, event.getMember(), queue.getVoiceChannel())) return;

        if(queue.getTrackScheduler().getQueue().isEmpty()) {
            Embeds.throwError(event, event.getUser(), "There aren't any items to shuffle.", true, null); return;
        }

        queue.getTrackScheduler().shuffle();
        event.reply(":white_check_mark: Successfully shuffled the queue. The next song is now `" + queue.getTrackScheduler().getQueue().get(0).getInfo().title + "`").queue();
    }
}
