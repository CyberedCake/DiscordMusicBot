package net.cybercake.discordmusicbot;

import net.cybercake.discordmusicbot.commands.CommandManager;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This code will return {@code true} if an exception occurs
 */
public class PresetExceptions {

    public static void criticalRare(Exception exception, IReplyCallback event, String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("A critical error occurred!");
        embed.addField(message, " ", true);
        try {
            embed.addField("**Exact Exception**", "||`" + exception + "`||", false);
        } catch (IllegalArgumentException ignored) { // value is too long
            embed.setDescription("**Exact Exception**: ||`" + exception + "`||");
        }
        embed.setTimestamp(new Date().toInstant());
        embed.setColor(Colors.ERROR.get());
        Log.error("A critical error occurred at " + new SimpleDateFormat("MMM d, yyyy HH:mm:ss z"), exception);

        if(event.isAcknowledged()) event.getHook().editOriginalEmbeds(embed.build()).queue();
        else event.replyEmbeds(embed.build()).queue();
    }

    public static boolean memberNull(IReplyCallback event) {
        if(event.getMember() == null) {
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue();
            return true;
        }
        return false;
    }

    public static @Nullable MusicPlayer trackIsNotPlaying(IReplyCallback callback, Member member, boolean ephemeral) {
        if(!Main.musicPlayerManager.checkMusicPlayerExists(member.getGuild())) {
            Embeds.throwError(callback, member.getUser(), "There currently aren't any songs playing in this server.", ephemeral, null); return null;
        }

        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(member.getGuild());
        if(musicPlayer.getAudioPlayer().getPlayingTrack() == null) {
            Embeds.throwError(callback, member.getUser(), "There currently aren't any songs playing in this server.", ephemeral, null); return null;
        }
        return musicPlayer;
    }

    public static boolean isNotBotDeveloper(IReplyCallback callback, Member member) { // returns true if no bot developer is found
        if(!CommandManager.BOT_DEVELOPERS.contains(member.getIdLong())) {
            callback.replyEmbeds(new EmbedBuilder().setTitle("No permission!").setDescription("Only bot developers can execute this command.").setColor(Colors.ERROR.get()).build()).setEphemeral(true).queue();
            return true;
        }
        return false;
    }

    public static boolean isNotInVoiceChat(IReplyCallback callback, Member member, AudioChannelUnion channel) {
        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(channel)) {
            Embeds.throwError(callback, member.getUser(), "You must be in the voice chat to continue.", true, null); return true;
        }
        return false;
    }

}
