package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class Shutdown extends Command {

    public Shutdown() {
        super("shutdown-bot", "Completely powers off the bot.");
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(event.getMember() == null) { // member doesn't exist for some reason
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue(); return;
        }

        if(event.getMember().getIdLong() != 351410272256262145L) {
            event.replyEmbeds(new EmbedBuilder().setTitle("No permission!").setDescription("Only bot developers can execute this command.").setColor(new Color(255, 41, 41)).build()).setEphemeral(true).queue();
            return;
        }

        try {
            event.replyEmbeds(new EmbedBuilder().setTitle("Goodbye!").setDescription("Shutting down the bot...").setColor(new Color(62, 137, 255)).build()).setEphemeral(true).queue();
            Thread.sleep(1000L);
            Main.JDA.shutdown();
        } catch (Exception exception) {
            Embeds.throwError(event, event.getUser(), "Failed to shutdown the bot, try again later!", exception);
        }
    }
}
