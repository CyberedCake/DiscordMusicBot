package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.Queue;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class NowPlaying extends Command {

    public NowPlaying() {
        super("nowplaying", "Check what song is currently playing.");
        this.aliases = new String[]{"np"};
        this.registerButtonInteraction = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;
        Embeds.sendNowPlayingStatus(event, queue.getAudioPlayer().getPlayingTrack(), event.getGuild());
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.equalsIgnoreCase("now-playing")) return;
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;
        Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;
        Embeds.sendNowPlayingStatus(event, queue.getAudioPlayer().getPlayingTrack(), event.getGuild());
    }
}
