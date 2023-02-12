package net.cybercake.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Pair;
import net.cybercake.discordmusicbot.generalutils.TrackUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Date;

public class Embeds {

    public static void executeEmbed(IReplyCallback event, EmbedBuilder builder, boolean ephemeral) { executeEmbed(event, builder.build(), ephemeral); }

    public static void executeEmbed(IReplyCallback event, MessageEmbed embed, boolean ephemeral) {
        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(embed).queue();
        else
            event.replyEmbeds(embed).setEphemeral(ephemeral).queue();
    }



    public static void throwError(IReplyCallback event, @Nullable User user, String errorMessage, boolean ephemeral, @Nullable Exception exception) {
        executeEmbed(event, getErrorEmbed0(user, errorMessage), ephemeral);

        if(exception != null)
            Log.error(errorMessage, exception);
    }

    public static void throwError(IReplyCallback event, @Nullable User user, String errorMessage, @Nullable Exception exception) {
        throwError(event, user, errorMessage, false, exception);
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

    private static String extractImage(AudioTrackInfo info) {
        @Nullable String image = null;
        if(info.uri.contains("youtube.com"))
            image = "https://i3.ytimg.com/vi/" + info.identifier + "/maxresdefault.jpg";
        return image;
    }

    public static Pair<TextChannel, Long> sendSongPlayingStatus(AudioTrack track, Guild guild) {
        String image = extractImage(track.getInfo());

        EmbedBuilder builder = new EmbedBuilder();
        if(image != null)
            builder.setThumbnail(image);
        builder.setAuthor("\uD83C\uDFB6 Now Playing");
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.addField("Duration", TrackUtils.getFormattedDuration(track.getDuration()), true);
        if(track.getUserData() != null)
            builder.addField("Requested By", "<@" + track.getUserData(User.class).getId() + ">", true);
        builder.setTimestamp(new Date().toInstant());
        Message message;
        try {
            message = Main.queueManager.getGuildQueue(guild).getTextChannel().sendMessageEmbeds(builder.build()).addActionRow(Button.danger("skip-track-" + track.getIdentifier(), "Skip Track")).complete();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send now playing status in text channel for " + guild.getId() + " (" + guild.getName() + ")", exception);
        }
        return new Pair<>(message.getChannel().asTextChannel(), message.getIdLong());
    }

    public static void sendNowPlayingStatus(SlashCommandInteractionEvent event, AudioTrack track, Guild guild) {
        @Nullable String image = null;
        if(track.getInfo().uri.contains("youtube.com"))
            image = "https://i3.ytimg.com/vi/" + track.getIdentifier() + "/maxresdefault.jpg";

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(108, 221, 255));
        if(image != null)
            builder.setThumbnail(image);
        builder.setAuthor(track.getInfo().author);
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.setDescription(TrackUtils.getDuration(track.getPosition(), track.getDuration()));
        if(track.getUserData() != null)
            builder.addField("Requested By", "<@" + track.getUserData(User.class).getId() + ">", false);
        builder.setTimestamp(new Date().toInstant());

        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(builder.build()).queue();
        else
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }






}
