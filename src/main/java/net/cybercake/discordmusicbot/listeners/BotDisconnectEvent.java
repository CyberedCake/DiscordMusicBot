package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Date;

public class BotDisconnectEvent extends ListenerAdapter {

    public void handleSelfDisconnect(Member member, VoiceChannel channel, Queue queue, GuildVoiceUpdateEvent event) {
        TextChannel textChannel = queue.getTextChannel();
        if(
                event.getVoiceState().getChannel() != null
                && event.getVoiceState().getChannel().asVoiceChannel().equals(queue.getVoiceChannel())
        ) return; // in case the bot was summoned using /summon

        try {
            queue.destroy();
        } catch (Exception exception) {
            return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot disconnected or moved.");
        builder.setDescription("The bot has been disconnected or moved from the voice chat. The bot is now leaving the voice chat and clearing its queue.\n\n" +
                "If you wish to move the bot to another channel, please use </summon:1082169015448907776>!");
        builder.setColor(new Color(255, 152, 68));
        builder.setTimestamp(new Date().toInstant());
        textChannel.sendMessageEmbeds(builder.build()).queue();
    }

    public void handleOtherDisconnect(Member member, VoiceChannel channel, Queue queue, GuildVoiceUpdateEvent event) {

    }


    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() == null) return;

        Guild guild = event.getGuild();
        if(!Main.queueManager.checkQueueExists(guild)) return;

        Queue queue = Main.queueManager.getGuildQueue(guild);
        if(!queue.getVoiceChannel().equals(event.getChannelLeft().asVoiceChannel())) return;

        Member member = event.getMember();
        VoiceChannel channel = event.getChannelLeft().asVoiceChannel();
        if(event.getMember().getUser().equals(Main.JDA.getSelfUser()))
            handleSelfDisconnect(member, channel, queue, event);
        else
            handleOtherDisconnect(member, channel, queue, event);
    }
}
