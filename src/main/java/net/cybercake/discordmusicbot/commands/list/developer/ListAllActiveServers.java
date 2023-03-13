package net.cybercake.discordmusicbot.commands.list.developer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.TrackUtils;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import java.awt.*;
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
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("All Active Servers");
        builder.setDescription("The bot is active in (" + activeGuilds.size() + ") guild" + (activeGuilds.size() == 1 ? "" : "s"));
        for(Guild guild : activeGuilds) {
            try {
                Queue queue = Main.queueManager.getGuildQueue(guild);
                AudioTrack currentTrack = queue.getAudioPlayer().getPlayingTrack();
                builder.addField("**" + guild.getName() + "**", // this is simply so I can guage whether or not I can restart the bot or if it may be a while before I can
                        "URI: `" + currentTrack.getInfo().uri + "`" + "\n" +
                                "Duration: `" + TrackUtils.getFormattedDuration(currentTrack.getPosition()) + "/" + TrackUtils.getFormattedDuration(currentTrack.getDuration()) + "`" + "\n" +
                                "Queue Size: `" + queue.getTrackScheduler().getQueue().size() + "`" + "\n" +
                                "Channel: `" + queue.getVoiceChannel().getName() + "`" + "\n" +
                                "Active Users: `" + queue.getVoiceChannel().getMembers().stream().filter(member -> !member.getUser().isBot()).toList().size() + "`"
                        ,
                        false
                );
            } catch (Exception exception) {
                builder.addField(guild.getName(),
                        "**An error occurred in displaying:** `" + exception + "`",
                        false
                        );
            }
        }
        builder.setColor(new Color(205, 255, 172));

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }
}
