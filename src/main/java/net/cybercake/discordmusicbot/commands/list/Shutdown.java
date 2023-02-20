package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.queue.QueueManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("⚠️ The bot is restarting! ⚠️");
            builder.setDescription("The bot is currently restarting. Try again in a few minutes. Disconnecting from the voice chat...");
            builder.setColor(new Color(255, 152, 68));
            builder.setTimestamp(new Date().toInstant());
            List<Guild> shutdownFor = new ArrayList<>();
            for(Queue queue : Main.queueManager.getAllQueues().values()) {
                Log.info("Shutting down music bot for " + queue.getGuild().getId() + " (" + queue.getGuild().getName() + ")...");
                shutdownFor.add(queue.getGuild());
                queue.getTextChannel().sendMessageEmbeds(builder.build()).queue();
                queue.destroy();
            }
            event.replyEmbeds(new EmbedBuilder().setTitle("Goodbye!").setDescription("Shutting down the bot..." +
                    "\n" +
                    "The bot was shutdown for (" + shutdownFor.size() + ") guilds: " + String.join(", ", shutdownFor.stream().map(Guild::getName).toArray(String[]::new))
            ).setColor(new Color(62, 137, 255)).build()).setEphemeral(true).queue();
            Thread.sleep(1000L);
            Main.JDA.shutdown();
        } catch (Exception exception) {
            Embeds.throwError(event, event.getUser(), "Failed to shutdown the bot, try again later!", exception);
        }
    }
}
