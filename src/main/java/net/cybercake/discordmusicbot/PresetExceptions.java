package net.cybercake.discordmusicbot;

import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import javax.annotation.Nullable;

/**
 * This code will return {@code true} if an exception occurs
 */
public class PresetExceptions {

    public static boolean memberNull(SlashCommandInteractionEvent event) {
        if(event.getMember() == null) {
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue();
            return true;
        }
        return false;
    }

    public static @Nullable Queue trackIsNotPlaying(IReplyCallback callback, Member member, boolean ephemeral) {
        if(!Main.queueManager.checkQueueExists(member.getGuild())) {
            Embeds.throwError(callback, member.getUser(), "There currently aren't any songs playing in this server.", ephemeral, null); return null;
        }

        Queue queue = Main.queueManager.getGuildQueue(member.getGuild());
        if(queue.getAudioPlayer().getPlayingTrack() == null) {
            Embeds.throwError(callback, member.getUser(), "There currently aren't any songs playing in this server.", ephemeral, null); return null;
        }
        return queue;
    }

}
