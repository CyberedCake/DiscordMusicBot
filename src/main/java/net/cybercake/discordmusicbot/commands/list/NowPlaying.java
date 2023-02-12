package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class NowPlaying extends Command {

    public NowPlaying() {
        super("nowplaying", "Check what song is currently playing.", new String[]{"np"}, new OptionData[]{});
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Queue queue = Main.queueManager.getGuildQueue(event.getGuild());
        Embeds.sendNowPlayingStatus(queue.getAudioPlayer().getPlayingTrack(), event.getGuild());
    }
}
