package net.cybercake.discordmusicbot.commands.list.developer;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.Log;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListAllActiveServers extends Command {

    public ListAllActiveServers(){
        super("listallactiveservers", "List all active servers");
        this.permission = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(event.getMember() == null) { // member doesn't exist for some reason
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue(); return;
        }

        if(PresetExceptions.isNotBotDeveloper(event, Objects.requireNonNull(event.getMember()))) return;

        List<Guild> activeGuilds = Main.queueManager.getAllQueues().values().stream().map(Queue::getGuild).toList();
        event.replyEmbeds(new EmbedBuilder().setTitle("All Active Servers").setDescription(
                "The bot is active in (" + activeGuilds.size() + ") guild" + (activeGuilds.size() == 1 ? "" : "s") + ": " + String.join(", ", activeGuilds.stream().map(Guild::getName).toArray(String[]::new))
        ).setColor(new Color(205, 255, 172)).build()).setEphemeral(true).queue();
    }
}
