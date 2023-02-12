package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotDisconnectEvent extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() == null) return;


    }
}
