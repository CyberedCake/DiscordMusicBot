package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getChannel().getIdLong() != Main.MUSIC_CHANNEL_ID) return;

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
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("A critical error occurred!");
            embed.addField("**The command failed, try again later.**", " ", true);
            embed.addField("**Exact Exception**", "||`" + exception + "`||", false);
            embed.setTimestamp(new Date().toInstant());
            embed.setColor(new Color(186, 24, 19));
            event.replyEmbeds(embed.build()).queue();
            Log.error("A critical error occurred at " + new SimpleDateFormat("MMM d, yyyy HH:mm:ss z"), exception);
        }
    }

}
