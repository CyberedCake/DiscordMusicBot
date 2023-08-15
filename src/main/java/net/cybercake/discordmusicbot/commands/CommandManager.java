package net.cybercake.discordmusicbot.commands;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CommandManager extends ListenerAdapter {

    public final static List<Long> BOT_DEVELOPERS = new ArrayList<>(
            List.of(351410272256262145L)
    );

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {
            Log.info(event.getUser().getName() + " (" + event.getUser().getIdLong() + "): " + event.getCommandString());
            if(Main.MAINTENANCE && !BOT_DEVELOPERS.contains(event.getUser().getIdLong())) {
                Embeds.sendMaintenanceStatus(event, "Only bot developers can execute this command at this moment. ");
                return;
            }
            Command command = Command.getCommandFromName(event.getName(), true);
            if(command == null) {
                event.replyEmbeds(Embeds.getErrorEmbed(event.getUser(), "**You have entered a command that doesn't exist. Contact a staff member.**").build())
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if(command.requiresDjRole() && Command.requireDjRole(event, event.getMember()))
                return;
            command.command(event);
        } catch (Exception exception) {
            PresetExceptions.criticalRare(exception, event, "**This command failed, try again later.**");
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        try {
            Command command = Command.getCommandFromName(event.getName(), true);
            if(command == null) return;
            command.tab(event);
        } catch (Exception exception) {
            Log.warn("Failed to autocomplete command '" + event.getName() + "': " + exception);
        }
    }

    private <T> net.dv8tion.jda.api.interactions.commands.Command.Choice choiceOf(T t) {
        try {
            return switch(t.getClass().getSimpleName()) {
                case "String" -> new net.dv8tion.jda.api.interactions.commands.Command.Choice((String)t, (String)t);
                case "Byte", "Short", "Integer", "Long" -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(String.valueOf(t), Long.parseLong(String.valueOf(t)));
                case "Float", "Double" -> new net.dv8tion.jda.api.interactions.commands.Command.Choice(String.valueOf(t), Double.parseDouble(String.valueOf(t)));
                default -> throw new IllegalArgumentException(t.getClass().getCanonicalName() + " is not an option for a " + net.dv8tion.jda.api.interactions.commands.Command.Choice.class.getCanonicalName() + "! Try using a " + String.join(", ", Stream.of(String.class, Long.class, Double.class).map(Class::getCanonicalName).toList()));
            };
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to get choice of " + t.getClass().getCanonicalName() + " (value=" + t + "): " + exception);
        }
    }
}
