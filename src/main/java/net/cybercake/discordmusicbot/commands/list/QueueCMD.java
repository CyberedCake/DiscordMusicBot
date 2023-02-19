package net.cybercake.discordmusicbot.commands.list;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.Sort;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class QueueCMD extends Command {

    public QueueCMD() {
        super(
                "queue", "View the queue and upcoming songs."
        );
        this.aliases = new String[]{"nextsong"};
        this.optionData = new OptionData[]{new OptionData(OptionType.INTEGER, "page", "Change the page that you're viewing", false)};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        StringBuilder builder = new StringBuilder();
        builder.append("```glsl\nQueue Page -1/-1\n\n");
        builder.append(nextLineFormatting(queue, queue.getAudioPlayer().getPlayingTrack(), checkUser(queue.getAudioPlayer().getPlayingTrack())));
        Sort.reverseOrder(queue.getTrackScheduler().getQueue())
                .forEach(track ->
                        builder.append(nextLineFormatting(queue, track, checkUser(track)))
                )
        ;
        builder.append("\n").append("```");
        event.getHook().editOriginal(builder.toString()).queue();
    }

    private User checkUser(AudioTrack track) {
        return (track.getUserData(User.class) == null ? null : track.getUserData(User.class));
    }

    private String nextLineFormatting(net.cybercake.discordmusicbot.queue.Queue queue, AudioTrack track, @Nullable User user) {
        int trackNumber = queue.getTrackScheduler().getQueue().size() - queue.getTrackScheduler().getQueue().indexOf(track) + 1;
        if(trackNumber > queue.getTrackScheduler().getQueue().size()+1)
            trackNumber = 1;

        return (trackNumber == 1 ? "*" : " ") +
                (trackNumber) + ") " +
                track.getInfo().title +
                (user != null ? " - Requested By: " + user.getName() + "#" + user.getDiscriminator() : "") +
                "\n";
    }
}
