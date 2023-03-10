package net.cybercake.discordmusicbot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.generalutils.Pair;
import net.cybercake.discordmusicbot.generalutils.TrackUtils;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Date;

public class Embeds {

    public static Color ERROR_COLOR = new Color(186, 24, 19);


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
        builder.setColor(ERROR_COLOR);
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

    public static Pair<TextChannel, Long> sendSongPlayingStatus(AudioTrack track, Guild guild, long edit) {
        String image = extractImage(track.getInfo());
        Queue queue = Main.queueManager.getGuildQueue(guild);

        EmbedBuilder builder = new EmbedBuilder();
        if(image != null)
            builder.setThumbnail(image);
        builder.setAuthor((queue.getTrackScheduler().pause() ? "??? **CURRENTLY PAUSED** ???" : "\uD83C\uDFB6 Now Playing"));
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.addField("Duration", TrackUtils.getFormattedDuration(track.getDuration()), true);
        if(track.getUserData() != null)
            builder.addField("Requested By", "<@" + track.getUserData(User.class).getId() + ">", true);
        builder.addField("Artist", track.getInfo().author, true);
        builder.setTimestamp(new Date().toInstant());
        Message message;
        try {
            ItemComponent[] buttons = new ItemComponent[]{
                    Button.danger("skip-track-" + track.getIdentifier(), "Skip Track"),
                    (queue.getTrackScheduler().pause() ? Button.success("resume-nomsg", "Resume Track") : Button.primary("pause-nomsg", "Pause Track")),
                    Button.secondary("view-queue", "View Queue")
            };
            if(edit == -1L)
                message = Main.queueManager.getGuildQueue(guild).getTextChannel()
                        .sendMessageEmbeds(builder.build())
                        .addActionRow(buttons)
                        .complete();
            else
                message = queue.getTextChannel().editMessageEmbedsById(edit, builder.build()).setActionRow(buttons).complete();

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

    public static void sendMaintenanceStatus(IReplyCallback callback, String initialMessage) {
        callback.replyEmbeds(new EmbedBuilder().setTitle(":exclamation: Bot is under maintenance! :exclamation:")
                        .setDescription(initialMessage + "Check again later when maintenance mode is off.\n\n*If you believe this to be in error, please contact **CyberedCake#9221** on Discord immediately!*")
                        .setColor(new Color(255, 41, 41)).build())
                .setEphemeral(true)
                .queue();
    }






}
