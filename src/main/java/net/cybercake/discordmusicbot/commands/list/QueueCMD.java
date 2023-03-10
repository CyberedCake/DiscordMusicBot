package net.cybercake.discordmusicbot.commands.list;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Embeds;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

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
        this.registerButtonInteraction = true;
    }

    private static final int ITEMS_PER_PAGE = 10;

    private void handleQueueCMD(IReplyCallback callback, int page) {
        if(PresetExceptions.memberNull(callback)) return;
        assert callback.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(callback, callback.getMember(), true);
        if(queue == null) return;

        int maxPages = getMaxPages(callback.getGuild());
        List<String> items = new ArrayList<>();
        int fromIndex = (page*(ITEMS_PER_PAGE))-(ITEMS_PER_PAGE);
        int toIndex = (Math.min(page * (ITEMS_PER_PAGE), queue.getTrackScheduler().getQueue().size()));
        try {
            queue.getTrackScheduler().getQueue().subList(fromIndex, toIndex)
                    .forEach(track ->
                            items.add(nextLineFormatting(queue, track, checkUser(track)))
                    );
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Embeds.throwError(callback, callback.getUser(), "Invalid queue page ('" + page + "'): " + indexOutOfBoundsException, indexOutOfBoundsException); return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("```glsl\nQueue Page ")
                .append(page)
                .append("/")
                .append(maxPages)
                .append("\n\n");
        if(page == 1) builder.append(nextLineFormatting(queue, queue.getAudioPlayer().getPlayingTrack(), checkUser(queue.getAudioPlayer().getPlayingTrack())));
        items.forEach(builder::append);
        builder.append("\n").append("```");
        List<ItemComponent> buttons = new ArrayList<>();
        buttons.add(Button.secondary("queue-first-" + page, "??? First").withDisabled(page <= 1));
        buttons.add(Button.secondary("queue-previous-" + page, "?????? Previous").withDisabled(page <= 1));
        buttons.add(Button.secondary("queue-next-" + page, "Next ??????").withDisabled(page == maxPages));
        buttons.add(Button.secondary("queue-last-" + page, "Last ???").withDisabled(page == maxPages));

        WebhookMessageEditAction<Message> hook = callback.getHook().editOriginal(builder.toString());
        if(maxPages != 0)
            hook.setActionRow(buttons);
        hook.queue();
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        OptionMapping pageOption = event.getOption("page");
        int page = pageOption == null ? 1 : pageOption.getAsInt();

        handleQueueCMD(event, page);
    }

    public void viewQueue(ButtonInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        handleQueueCMD(event, 1);
    }

    @Override
    public void button(ButtonInteractionEvent event, String buttonId) {
        if(buttonId.equalsIgnoreCase("view-queue")) { viewQueue(event); return; }
        if(!buttonId.contains("queue-")) return;
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        net.cybercake.discordmusicbot.queue.Queue queue = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(queue == null) return;

        int page = Integer.parseInt(event.getComponentId().split("-")[2]);
        event.deferEdit().queue();
        switch(event.getComponentId().split("-")[1]) {
            case "first" -> handleQueueCMD(event, 1);
            case "previous" -> handleQueueCMD(event, page-1);
            case "next" -> handleQueueCMD(event, page+1);
            case "last" -> handleQueueCMD(event, getMaxPages(event.getGuild()));
            default -> throw new IllegalStateException("Invalid state of component ID for " + Button.class.getCanonicalName() + "... found " + event.getComponentId().split("-")[1]);
        }
    }

    private int getMaxPages(Guild guild) {
        return (int)Math.ceil((double)Main.queueManager.getGuildQueue(guild).getTrackScheduler().getQueue().size()/(double)(ITEMS_PER_PAGE));
    }

    private User checkUser(AudioTrack track) {
        return (track.getUserData(User.class) == null ? null : track.getUserData(User.class));
    }

    private String nextLineFormatting(net.cybercake.discordmusicbot.queue.Queue queue, AudioTrack track, @Nullable User user) {
        int trackNumber = queue.getTrackScheduler().getQueue().indexOf(track) + 1;
        if(trackNumber > queue.getTrackScheduler().getQueue().size())
            trackNumber = 0;

        return " " +
                (trackNumber == 0 ? "Playing" : trackNumber) + ") " +
                track.getInfo().title +
                (user != null ? " - Requested By: " + user.getName() + "#" + user.getDiscriminator() : "") +
                "\n" + (trackNumber == 0 ? "\n" : "");
    }

    @Override
    @SuppressWarnings({"all"})
    public List<Integer> tab(CommandAutoCompleteInteractionEvent event) {
        return IntStream.range(0, getMaxPages(event.getGuild())+1).boxed().toList();
    }
}
