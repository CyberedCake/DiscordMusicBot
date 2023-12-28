package net.cybercake.discordmusicbot.listeners;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.commands.list.admin.Summon;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Date;
import java.util.Objects;

public class BotDisconnectEvent extends ListenerAdapter {

    public void handleSelfDisconnect(Member member, AudioChannelUnion channel, MusicPlayer musicPlayer, GuildVoiceUpdateEvent event) {
        TextChannel textChannel = musicPlayer.getTextChannel();
        if(
                event.getVoiceState().getChannel() != null
                && event.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())
        ) return; // in case the bot was summoned using /summon

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot disconnected or moved.");
        builder.setDescription("The bot has been disconnected or moved from the voice chat. The bot is now leaving the voice chat and clearing its queue.\n\n" +
                "If you wish to move the bot to another channel, please use " + Command.getCommandClass(Summon.class).getJdaCommand().getAsMention() + "!");
        builder.setColor(Colors.DISCONNECTED.get());
        builder.setTimestamp(new Date().toInstant());
        textChannel.sendMessageEmbeds(builder.build()).queue();

        Thread waitTime = new Thread(() -> {
            try {
                musicPlayer.getTrackScheduler().pause(true);
                Thread.sleep(1000);
                musicPlayer.destroy();
            } catch (Exception ignored) { }
        });
        waitTime.start();
    }

    public void handleOtherDisconnect(Member member, AudioChannelUnion channel, MusicPlayer musicPlayer, GuildVoiceUpdateEvent event) {

    }


    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(event.getChannelLeft() == null) return;

        Guild guild = event.getGuild();
        if(!Main.musicPlayerManager.checkMusicPlayerExists(guild)) return;

        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(guild);
        if(!musicPlayer.getVoiceChannel().equals(event.getChannelLeft())) return;

        Member member = event.getMember();
        AudioChannelUnion channel = event.getChannelLeft();
        if(event.getMember().getUser().equals(Main.JDA.getSelfUser()))
            handleSelfDisconnect(member, channel, musicPlayer, event);
        else
            handleOtherDisconnect(member, channel, musicPlayer, event);
    }
}
