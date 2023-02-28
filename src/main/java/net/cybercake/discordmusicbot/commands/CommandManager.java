package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    public final static List<Long> BOT_DEVELOPERS = new ArrayList<>(
            List.of(351410272256262145L)
    );

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            Log.info(event.getUser().getName() + "#" + event.getUser().getDiscriminator() + " (" + event.getUser().getIdLong() + "): " + event.getCommandString());
            Command command = Command.getCommandFromName(event.getName(), true);
            if(command == null) {
                event.replyEmbeds(Embeds.getErrorEmbed(event.getUser(), "**You have entered a command that doesn't exist. Contact a staff member.**").build())
                        .setEphemeral(true)
                        .queue();
                return;
            }
            command.command(event);
        } catch (Exception exception) {
            PresetExceptions.criticalRare(exception, event, "**This command failed, try again later.**");
        }
    }

}
