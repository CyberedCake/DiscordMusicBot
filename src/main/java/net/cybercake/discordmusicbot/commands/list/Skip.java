package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
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
        this.aliases = new String[]{"s"};
        this.registerButtonInteraction = true;
        this.requireDjRole = true;
    }

    public static void handleSkip(MusicPlayer musicPlayer, Member member, IReplyCallback callback, String identifier) {
        if(!musicPlayer.getAudioPlayer().getPlayingTrack().getIdentifier().equalsIgnoreCase(identifier)) {
            Embeds.throwError(callback, member.getUser(), "That song is no longer playing.", true, null); return;
        }

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(callback, member.getUser(), "You must be in the voice chat to skip a song", true, null); return;
        }

        if(musicPlayer.getSkipSongManager().hasMemberSkipVoted(member)) {
            Embeds.throwError(callback, member.getUser(), "You already voted to skip this song.", true, null); return;
        }

        musicPlayer.getSkipSongManager().addMemberToSkipVote(member);

        callback.deferReply().setEphemeral(true).complete().deleteOriginal().queue();

        TextChannel channel = (TextChannel) callback.getMessageChannel();
        if(musicPlayer.getSkipSongManager().getUsersWhoCanSkip().size() > 1)
            channel.sendMessage(member.getAsMention() + " has requested to skip `" + musicPlayer.getAudioPlayer().getPlayingTrack().getInfo().title + "`. **[" + musicPlayer.getSkipSongManager().getMembersWantingSkip() + "/" + musicPlayer.getSkipSongManager().getMaxNeededToSkip() + " - " + (musicPlayer.getSkipSongManager().getMembersWantingSkipPercent()) + "%]**" + (musicPlayer.getSkipSongManager().getMembersWantingSkipPercent() >= 100 ? "\n\n*Skipping this song...*" : "")).queue();
        musicPlayer.getSkipSongManager().checkSkipProportion();
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;
        Skip.handleSkip(musicPlayer, event.getMember(), event, Main.musicPlayerManager.getGuildQueue(event.getGuild()).getAudioPlayer().getPlayingTrack().getIdentifier());
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(!buttonId.contains("skip-track-")) return;
        Skip.handleSkip(Main.musicPlayerManager.getGuildQueue(event.getGuild()), event.getMember(), event, event.getComponentId().replace("skip-track-", ""));
    }
}
