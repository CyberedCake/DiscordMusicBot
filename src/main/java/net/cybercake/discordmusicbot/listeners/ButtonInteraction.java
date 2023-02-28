package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.commands.list.Skip;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ButtonInteraction extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        try {
            if(Main.MAINTENANCE && !CommandManager.BOT_DEVELOPERS.contains(event.getUser().getIdLong())) {
                Embeds.sendMaintenanceStatus(event, "Only bot developers can execute button interactions at this moment. ");
                return;
            }
            if(!event.getComponentId().contains("skip-track-")) return;
            Skip.handleSkip(Main.queueManager.getGuildQueue(event.getGuild()), event.getMember(), event, event.getComponentId().replace("skip-track-", ""));
        } catch (Exception exception) {
            PresetExceptions.criticalRare(exception, event, "**This button interaction failed, try again later.**");
        }
    }
}
