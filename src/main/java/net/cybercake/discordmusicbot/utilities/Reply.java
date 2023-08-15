package net.cybercake.discordmusicbot.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Reply {

    public static void embed(IReplyCallback event, EmbedBuilder embed, boolean ephemeral) {
        Embeds.executeEmbed(event, embed, ephemeral);
    }

    public static void embed(IReplyCallback event, MessageEmbed embed, boolean ephemeral) {
        Embeds.executeEmbed(event, embed, ephemeral);
    }

    public static void message(IReplyCallback event, String message, boolean ephemeral) {
        if(event.isAcknowledged())
            event.getHook().editOriginal(message).queue();
        else event.reply(message).setEphemeral(ephemeral).queue();
    }

}
