package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ButtonInteraction extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            if(Main.MAINTENANCE && !CommandManager.BOT_DEVELOPERS.contains(event.getUser().getIdLong())) {
                Embeds.sendMaintenanceStatus(event, "Only bot developers can execute button interactions at this moment. ");
                return;
            }

            Command.getCommands().stream().filter(Command::sendButtonInteractionEvent).forEach(cmd -> {
                if(cmd.requiresDjRole() && Command.requireDjRole(event, event.getMember()))
                    return;
                cmd.button(event, event.getComponentId());
            });
        } catch (Exception exception) {
            PresetExceptions.criticalRare(exception, event, "**This button interaction failed, try again later.**");
        }
    }
}
