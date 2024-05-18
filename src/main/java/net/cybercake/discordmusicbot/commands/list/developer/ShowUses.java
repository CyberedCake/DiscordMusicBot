package net.cybercake.discordmusicbot.commands.list.developer;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ShowUses extends Command {

    public ShowUses(){
        super("showuses", "List all active servers");
        this.permission = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR);
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(event.getMember() == null) { // member doesn't exist for some reason
            event.getHook().editOriginalEmbeds(Embeds.getTechnicalErrorEmbed(null, "member == null").build()).queue(); return;
        }

        if(PresetExceptions.isNotBotDeveloper(event, Objects.requireNonNull(event.getMember()))) return;

        List<Guild> activeGuilds = Main.musicPlayerManager.getAllMusicPlayers().values().stream().map(MusicPlayer::getGuild).toList();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("All Active Servers");
        builder.setDescription("The bot is active in (" + activeGuilds.size() + ") guild" + (activeGuilds.size() == 1 ? "" : "s"));
        for(Guild guild : activeGuilds) {
            try {
                List<?> data = new LinkedList<>(Main.musicPlayerManager.getGuildMusicPlayer(guild).data().values());
                Log.info(data.toString());
                builder.addField("Hash #**" + data.get(0) + "** (" + guild.getId() + ")", // this is simply so I can guage whether or not I can restart the bot or if it may be a while before I can
                        "id: `" + data.get(1) + "`" + "\n" +
                                "times: `" + TrackUtils.getFormattedDuration((long)data.get(2)) + "/" + TrackUtils.getFormattedDuration((long)data.get(3)) + "`" + "\n" +
                                "queue_size: `" + data.get(4) + "`" + "\n" +
                                "active_users: `" + data.get(5) + "`"
                        ,
                        false
                );
            } catch (Exception exception) {
                Log.warn("An error occurred", exception);
                builder.addField(guild.getName(),
                        "**An error occurred in displaying:** `" + exception + "`",
                        false
                        );
            }
        }
        builder.setColor(Colors.LIST.get());

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }
}
