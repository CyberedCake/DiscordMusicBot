package net.cybercake.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Pair;
import net.cybercake.discordmusicbot.generalutils.TrackUtils;
import net.cybercake.discordmusicbot.queue.QueueManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
        builder.setAuthor(track.getInfo().author);
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.addField("Duration", TrackUtils.getFormattedDuration(track.getDuration()), true);
        builder.addField("Requested By", "null", true);
        builder.setTimestamp(new Date().toInstant());
        AtomicReference<Message> message = new AtomicReference<>(null);
        Main.queueManager.getGuildQueue(guild).getTextChannel().sendMessageEmbeds(builder.build()).queue(
                message::set
        );
        if(message.get() == null)
            throw new IllegalStateException("Message wasn't sent or an error occurred");
        return new Pair<>(message.get().getChannel().asTextChannel(), message.get().getIdLong());
    }

    public static Message sendNowPlayingStatus(AudioTrack track, Guild guild) {
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
        builder.addField("Requested By", "null", false);
        builder.setTimestamp(new Date().toInstant());
        AtomicReference<Message> message = new AtomicReference<>(null);
        Main.queueManager.getGuildQueue(guild).getTextChannel().sendMessageEmbeds(builder.build()).queue(
                message::set
        );
        return message.get();
    }






}
