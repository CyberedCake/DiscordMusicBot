package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.list.Pause;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.queue.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

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
        builder.setColor(Colors.ERROR.get());
        builder.setTitle("An error occurred!");
        builder.setDescription(errorMessage + "\n \r");
        builder.setTimestamp(new Date().toInstant());
        if(user != null)
            builder.setFooter("Response to " + user.getName());
        return builder;
    }

    public static MessageEmbed getErrorEmbed0(@Nullable User user, String errorMessage) {
        return getErrorEmbed(user, errorMessage).build();
    }

    public static EmbedBuilder getTechnicalErrorEmbed(@Nullable User user, String technicalInformation) {
        return getErrorEmbed(user, "`" + technicalInformation + "` -- That's all we know, please contact a staff member");
    }

    public static Pair<TextChannel, Long> sendSongPlayingStatus(AudioTrack track, Guild guild, long edit) {
        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(guild);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(YouTubeUtils.getArtwork(track.getInfo()));
        builder.setColor(Colors.CURRENT_SONG_STATUS.get());
        builder.setAuthor((musicPlayer.getTrackScheduler().pause() ? "⏸ **CURRENTLY PAUSED** ⏸" : "\uD83C\uDFB5 Now Playing"));
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.addField("Duration", TrackUtils.getFormattedDuration(track.getDuration()), true);
        if(!musicPlayer.getTrackScheduler().pause() && track.getUserData() != null)
            builder.addField("Requested By", "<@" + TrackUtils.deserializeUserData(track.getUserData()).getFirstItem().getId() + ">", true);
        if(musicPlayer.getTrackScheduler().pause())
            builder.addField("Paused By", "<@" + Pause.lastPauser.getId() + ">", true);
        if(musicPlayer.getTrackScheduler().repeating() != TrackScheduler.Repeating.FALSE)
            builder.addField("Looping", musicPlayer.getTrackScheduler().repeating().userFriendlyString(), true);
        builder.addField("Artist", track.getInfo().author, true);
        Message message;
        try {
            Button skipButton = Button.secondary("skip-track-" + track.getIdentifier(), Emoji.fromFormatted("⏭"));

            Queue queue = musicPlayer.getTrackScheduler().getQueue();
            if (queue.getCurrentIndex() >= queue.getLiteralQueue().size())
                skipButton = skipButton.asDisabled();

            Button backButton = Button.secondary("previous-track-" + track.getIdentifier(), Emoji.fromFormatted("⏮"));
            if (queue.getCurrentIndex() <= 1)
                backButton = backButton.asDisabled();

            ItemComponent[][] buttons = new ItemComponent[][]{
                    new ItemComponent[]{
                            backButton,
                            Button.secondary("pauseresume", Emoji.fromFormatted("⏸")),
                            skipButton
                    },
                    new ItemComponent[]{
                            Button.secondary("loop-song", Emoji.fromFormatted("\uD83D\uDD02")),
                            Button.danger("stop-queue", Emoji.fromFormatted("\uD83D\uDED1")),
                            Button.secondary("shuffle-queue", Emoji.fromFormatted("\uD83D\uDD00"))
                    },
                    new ItemComponent[]{
                            Button.secondary("volume:0", Emoji.fromFormatted("\uD83D\uDD07")),
                            Button.secondary("volume:50", Emoji.fromFormatted("\uD83D\uDD09")),
                            Button.secondary("volume:100", Emoji.fromFormatted("\uD83D\uDD0A"))
                    },
                    new ItemComponent[]{
                            Button.secondary("view-queue", "View Queue"),
                            Button.secondary("now-playing", "More Details")
                    }
            };

            if(edit == -1L) {
                MessageCreateAction create = Main.musicPlayerManager.getGuildMusicPlayer(guild).getTextChannel()
                        .sendMessageEmbeds(builder.build());
                for(ItemComponent[] component : buttons)
                    create.addActionRow(component);
                message = create.complete();
            } else
                message = musicPlayer.getTextChannel().editMessageEmbedsById(edit, builder.build()).complete();

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send now playing status in text channel for " + guild.getId() + " (" + guild.getName() + ")", exception);
        }
        return new Pair<>(message.getChannel().asTextChannel(), message.getIdLong());
    }

    public static void sendNowPlayingStatus(IReplyCallback event, AudioTrack track) {
        int percentage = NumberUtils.asRoundedInt((((double)track.getPosition() / (double)track.getDuration()) * 100D));

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Colors.NOW_PLAYING.get());
        builder.setThumbnail(YouTubeUtils.getArtwork(track.getInfo()));
        builder.setAuthor(track.getInfo().author);
        builder.setTitle(track.getInfo().title, track.getInfo().uri);
        builder.setDescription(TrackUtils.getDuration(track.getPosition(), track.getDuration()) + "   (**" + percentage + "%**)");
        if(track.getUserData() != null)
            builder.addField("Requested By", "<@" + TrackUtils.deserializeUserData(track.getUserData()).getFirstItem().getId() + ">", false);

        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(builder.build()).queue();
        else
            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    public static void sendMaintenanceStatus(IReplyCallback callback, String initialMessage) {
        callback.replyEmbeds(new EmbedBuilder().setTitle(":exclamation: Bot is under maintenance! :exclamation:")
                        .setDescription(initialMessage + "Check again later when maintenance mode is off.\n\n*If you believe this to be in error, please contact **CyberedCake#9221** on Discord immediately!*")
                        .setColor(Colors.ERROR.get()).build())
                .setEphemeral(true)
                .queue();
    }






}
