package net.cybercake.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Date;

public class Embeds {

    public static void executeEmbed(SlashCommandInteractionEvent event, EmbedBuilder builder) { executeEmbed(event, builder.build()); }

    public static void executeEmbed(SlashCommandInteractionEvent event, MessageEmbed embed) {
        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(embed).queue();
        else
            event.replyEmbeds(embed).queue();
    }



    public static void throwError(SlashCommandInteractionEvent event, @Nullable User user, String errorMessage, @Nullable Exception exception) {
        executeEmbed(event, getErrorEmbed0(user, errorMessage));

        if(exception != null)
            Log.error(errorMessage, exception);
    }

    public static EmbedBuilder getErrorEmbed(@Nullable User user, String errorMessage) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(186, 24, 19));
        builder.setTitle("An error occurred!");
        builder.setDescription(errorMessage + "\n \r");
        builder.setTimestamp(new Date().toInstant());
        if(user != null)
            builder.setFooter("Requested by " + user.getName() + "#" + user.getDiscriminator());
        return builder;
    }

    public static MessageEmbed getErrorEmbed0(@Nullable User user, String errorMessage) {
        return getErrorEmbed(user, errorMessage).build();
    }

    public static EmbedBuilder getTechnicalErrorEmbed(@Nullable User user, String technicalInformation) {
        return getErrorEmbed(user, "`" + technicalInformation + "` -- That's all we know, please contact a staff member");
    }

    public static void sendSongPlayingStatus(SlashCommandInteractionEvent event, AudioTrack track, AudioLoadResultHandler audioLoadResultHandler) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(137, 255, 108));
        Log.info(track.getIdentifier());
        builder.setTitle(track.getInfo().title, "https://www.youtube.com/watch?v=" + track.getIdentifier());
        builder.setDescription("Length: " + track.getDuration());
        builder.setTimestamp(new Date().toInstant());
        builder.setFooter("Requested by *soon*");
        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(builder.build()).queue();
        else
            event.replyEmbeds(builder.build()).queue();
    }






}
