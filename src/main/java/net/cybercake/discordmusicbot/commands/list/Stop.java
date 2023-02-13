package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;

public class Stop extends Command {

    public Stop() {
        super(
                "stop", "Removes the bot from the voice chat."
        );
        this.aliases = new String[]{"disconnect", "leave", "remove", "goodbye"};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        event.deferReply().queue();
        queue.destroy();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot disconnected.");
        builder.setDescription(event.getMember().getAsMention() + " cleared the queue and disconnected the bot from the voice chat.");
        builder.setColor(new Color(255, 152, 68));
        builder.setTimestamp(new Date().toInstant());
        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }
}
