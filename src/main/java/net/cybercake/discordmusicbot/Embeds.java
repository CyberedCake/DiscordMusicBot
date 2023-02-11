package net.cybercake.discordmusicbot;

import net.cybercake.discordmusicbot.generalutils.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Date;

public class Embeds {

    public static void throwError(SlashCommandInteractionEvent event, @Nullable User user, String errorMessage, @Nullable Exception exception) {
        if(event.isAcknowledged())
            event.getHook().editOriginalEmbeds(getErrorEmbed0(user, errorMessage)).queue();
        else
            event.replyEmbeds(getErrorEmbed0(user, errorMessage)).setEphemeral(true).queue();

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
}
