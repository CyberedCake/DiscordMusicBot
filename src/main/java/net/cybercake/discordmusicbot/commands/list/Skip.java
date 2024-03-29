package net.cybercake.discordmusicbot.commands.list;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.seek.SeekQueueVoteFromCommand;
import net.cybercake.discordmusicbot.queue.seek.SeekType;
import net.cybercake.discordmusicbot.queue.seek.SongQueueSeekManager;
import net.cybercake.discordmusicbot.queue.seek.VoteForTrack;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.cybercake.discordmusicbot.utilities.YouTubeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class Skip extends Command {

    public Skip() {
        super(
                "skip", "Skips the current track to the next track via a vote."
        );
        this.aliases = new String[]{"s", "nextsong"};
        this.registerButtonInteraction = true;
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;
        SeekQueueVoteFromCommand.handle(SeekType.NEXT, musicPlayer, event.getMember(), event, Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild()).getAudioPlayer().getPlayingTrack().getIdentifier());
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("skip-track-")) return;
        SeekQueueVoteFromCommand.handle(SeekType.NEXT, Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild()), event.getMember(), event, event.getComponentId().replace("skip-track-", ""));
    }
}
