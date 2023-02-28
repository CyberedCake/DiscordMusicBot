package net.cybercake.discordmusicbot.commands.list;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.generalutils.NumberUtils;
import net.cybercake.discordmusicbot.generalutils.Sort;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class QueueCMD extends Command {

    public QueueCMD() {
        super(
                "queue", "View the queue and upcoming songs."
        );
        this.aliases = new String[]{"nextsong"};
        this.optionData = new OptionData[]{new OptionData(OptionType.INTEGER, "page", "Change the page that you're viewing", false, true)};
    }

    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        OptionMapping pageOption = event.getOption("page");
        int page = pageOption == null ? 1 : pageOption.getAsInt();

        List<String> items = new ArrayList<>();
        queue.getTrackScheduler().getQueue().subList((page*(ITEMS_PER_PAGE-1))-(ITEMS_PER_PAGE-1), (Math.min(page * (ITEMS_PER_PAGE - 1), queue.getTrackScheduler().getQueue().size())))
                .forEach(track ->
                        items.add(nextLineFormatting(queue, track, checkUser(track)))
                );

        StringBuilder builder = new StringBuilder();
        builder.append("```glsl\nQueue Page ")
                .append(page)
                .append("/")
                .append(getMaxPages(event.getGuild()))
                .append("\n\n");
        if(page == 1) builder.append(nextLineFormatting(queue, queue.getAudioPlayer().getPlayingTrack(), checkUser(queue.getAudioPlayer().getPlayingTrack())));
        items.forEach(builder::append);
        builder.append("\n").append("```");
        event.getHook().editOriginal(builder.toString()).queue();
    }

    private int getMaxPages(Guild guild) {
        return (int)Math.ceil((double)Main.queueManager.getGuildQueue(guild).getTrackScheduler().getQueue().size()/(double)(ITEMS_PER_PAGE-1));
    }

    private User checkUser(AudioTrack track) {
        return (track.getUserData(User.class) == null ? null : track.getUserData(User.class));
    }

    private String nextLineFormatting(net.cybercake.discordmusicbot.queue.Queue queue, AudioTrack track, @Nullable User user) {
        int trackNumber = queue.getTrackScheduler().getQueue().indexOf(track) + 2;
        if(trackNumber > queue.getTrackScheduler().getQueue().size()+1)
            trackNumber = 1;

        return (trackNumber == 1 ? "*" : " ") +
                (trackNumber) + ") " +
                track.getInfo().title +
                (user != null ? " - Requested By: " + user.getName() + "#" + user.getDiscriminator() : "") +
                "\n";
    }

    @Override
    @SuppressWarnings({"all"})
    public List<Integer> tab(CommandAutoCompleteInteractionEvent event) {
        return IntStream.range(0, getMaxPages(event.getGuild())+1).boxed().toList();
    }
}
