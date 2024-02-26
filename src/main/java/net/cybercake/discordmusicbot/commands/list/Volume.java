package net.cybercake.discordmusicbot.commands.list;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.swing.text.html.Option;
import java.util.List;

public class Volume extends Command {

    public Volume() {
        super(
                "volume", "Sets the volume of the music bot."
        );
        this.optionData = new OptionData[]{new OptionData(OptionType.NUMBER, "volume", "The new volume of the bot. 100% is the default.", false).setRequiredRange(0.0, 200.0).setMaxValue(200.0).setMinValue(0.0)};
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        Member member = event.getMember();
        assert member != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null || !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(event, member.getUser(), "You must be in the voice chat to pause the song", true, null); return;
        }

        if(musicPlayer.getTrackScheduler().pause()) {
            Embeds.throwError(event, member.getUser(), "The queue is already paused, use " + Command.getCommandClass(Resume.class).getJdaCommand().getAsMention() + " to continue the song", true, null); return;
        }

        OptionMapping option = event.getOption("volume");
        if (option == null) {
            int volume = musicPlayer.getAudioPlayer().getVolume();
            event.replyEmbeds(new EmbedBuilder()
                            .setColor(Colors.VOLUME.get())
                            .setTitle("The server-side volume is currently " + volume + "%" + (volume == 100 ? " (default)" : ""))
                            .build()
            ).queue();
            return;
        }

        int volume = (int) option.getAsDouble();
        musicPlayer.getAudioPlayer().setVolume(volume);
        event.replyEmbeds(new EmbedBuilder()
                .setColor(Colors.VOLUME.get())
                .setDescription(member.getAsMention() + " set the server-side volume to " + volume + "%" + (volume == 100 ? " (default)" : ""))
                .build()).queue();
    }
}
