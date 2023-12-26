package net.cybercake.discordmusicbot.commands.list.developer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
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

        List<Guild> activeGuilds = Main.musicPlayerManager.getAllMusicPlayers().values().stream().map(MusicPlayer::getGuild).toList();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("All Active Servers");
        builder.setDescription("The bot is active in (" + activeGuilds.size() + ") guild" + (activeGuilds.size() == 1 ? "" : "s"));
        for(Guild guild : activeGuilds) {
            try {
                MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayer(guild);
                AudioTrack currentTrack = musicPlayer.getAudioPlayer().getPlayingTrack();
                builder.addField("**" + guild.getName() + "**", // this is simply so I can guage whether or not I can restart the bot or if it may be a while before I can
                        "URI: `" + currentTrack.getInfo().uri + "`" + "\n" +
                                "Duration: `" + TrackUtils.getFormattedDuration(currentTrack.getPosition()) + "/" + TrackUtils.getFormattedDuration(currentTrack.getDuration()) + "`" + "\n" +
                                "Queue Size: `" + musicPlayer.getTrackScheduler().getQueue().getLiteralQueue().size() + "`" + "\n" +
                                "Channel: `" + musicPlayer.getVoiceChannel().getName() + "`" + "\n" +
                                "Active Users: `" + musicPlayer.getVoiceChannel().getMembers().stream().filter(member -> !member.getUser().isBot()).toList().size() + "`"
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
        builder.setColor(Colors.LIST.get());

        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }
}
