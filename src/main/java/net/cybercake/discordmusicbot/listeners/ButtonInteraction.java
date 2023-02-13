package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.list.Skip;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ButtonInteraction extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(!event.getComponentId().contains("skip-track-")) return;
        Skip.handleSkip(Main.queueManager.getGuildQueue(event.getGuild()), event.getMember(), event, event.getComponentId().replace("skip-track-", ""));
    }
}
