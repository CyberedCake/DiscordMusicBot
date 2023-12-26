package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.seek.SeekQueueVoteFromCommand;
import net.cybercake.discordmusicbot.queue.seek.SeekType;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Previous extends Command {

    public Previous() {
        super(
                "previous", "Brings the queue back to the previous song via a vote."
        );
        this.aliases = new String[]{"back", "b"};
        this.registerButtonInteraction = true;
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;
        SeekQueueVoteFromCommand.handle(SeekType.PREVIOUS, musicPlayer, event.getMember(), event, Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild()).getAudioPlayer().getPlayingTrack().getIdentifier());
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("previous-track-")) return;
        SeekQueueVoteFromCommand.handle(SeekType.PREVIOUS, Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild()), event.getMember(), event, event.getComponentId().replace("previous-track-", ""));
    }
}
