package net.cybercake.discordmusicbot.commands.list;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.cybercake.discordmusicbot.utilities.YouTubeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.LongStream;

public class QueueCMD extends Command {

    public QueueCMD() {
        super(
                "queue", "View the queue and upcoming songs."
        );
        this.aliases = new String[]{"nextsong"};
        this.optionData = new OptionData[]{
                new OptionData(OptionType.INTEGER, "page", "Change the page that you're viewing", false, true)
        };
        this.registerButtonInteraction = true;
    }

    private static final int ITEMS_PER_PAGE = 6;

    private void handleQueueCMD(IReplyCallback callback, int page) {
        if(PresetExceptions.memberNull(callback)) return;
        assert callback.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(callback, callback.getMember(), true);
        if(musicPlayer == null) return;

        int maxPages = getMaxPages(callback.getGuild());
        List<AudioTrack> items;
        int fromIndex = (page*(ITEMS_PER_PAGE))-(ITEMS_PER_PAGE);
        int toIndex = (Math.min(page * (ITEMS_PER_PAGE), musicPlayer.getTrackScheduler().getQueue().size()));
        try {
            items = new ArrayList<>(musicPlayer.getTrackScheduler().getQueue().subList(fromIndex, toIndex));
        } catch (Exception exception) {
            Embeds.throwError(callback, callback.getUser(), "Possible invalid queue page ('" + page + "'): " + exception, exception); return;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Queue Page " + page + " / " + maxPages);

        AudioTrack currentTrack = musicPlayer.getAudioPlayer().getPlayingTrack();
        builder.setThumbnail(YouTubeUtils.extractImage(currentTrack.getInfo()));
        builder.addField("Currently Playing", nextLineFormatting(musicPlayer, currentTrack, checkUser(currentTrack)), false);

        StringJoiner upNext = new StringJoiner("\n");
        items.stream().map(track -> nextLineFormatting(musicPlayer, track, checkUser(track))).forEach(upNext::add);
        if(!upNext.toString().isEmpty())
            builder.addField("Up Next", upNext.toString(), false);

        builder.setColor(new Color(0, 65, 59));

        List<ItemComponent> buttons = new ArrayList<>();
        buttons.add(Button.secondary("queue-first-" + page, "⏪ First").withDisabled(page <= 1));
        buttons.add(Button.secondary("queue-previous-" + page, "◀️ Previous").withDisabled(page <= 1));
        buttons.add(Button.secondary("queue-next-" + page, "Next ▶️").withDisabled(page == maxPages));
        buttons.add(Button.secondary("queue-last-" + page, "Last ⏩").withDisabled(page == maxPages));

        WebhookMessageEditAction<Message> hook = callback.getHook().editOriginalEmbeds(builder.build());
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

    private int getTrackIndexOf(MusicPlayer musicPlayer, AudioTrack track) {
        int trackNumber = musicPlayer.getTrackScheduler().getQueue().indexOf(track) + 1;
        if(trackNumber > musicPlayer.getTrackScheduler().getQueue().size())
            trackNumber = 0;
        return trackNumber;
    }

    private String nextLineFormatting(MusicPlayer musicPlayer, AudioTrack track, @Nullable User user) {
        int trackNumber = getTrackIndexOf(musicPlayer, track);

        int position = (int)(track.getPosition() / 1000);
        String positionQuery = position == 0 ? "" : "&t=" + position;
        return (trackNumber == 0 ? "\t" : trackNumber + ". ") +
                "[" + track.getInfo().title + "](https://www.youtube.com/watch?v=" + track.getIdentifier() + positionQuery + ") " +
                (user == null ? "" : "(Requested by **" + user.getName() + "**)");
//        return " " +
//                (trackNumber == 0 ? "Playing" : trackNumber) + ") " +
//                track.getInfo().title +
//                (user != null ? " - Requested By: " + user.getName() + "#" + user.getDiscriminator() : "") +
//                "\n" + (trackNumber == 0 ? "\n" : "");
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

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

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
        return (int)Math.ceil((double)Main.musicPlayerManager.getGuildQueue(guild).getTrackScheduler().getQueue().size()/(double)(ITEMS_PER_PAGE));
    }

    private User checkUser(AudioTrack track) {
        return (track.getUserData() == null ? null : TrackUtils.deserializeUserData(track.getUserData()).getFirstItem());
    }

    @Override
    @SuppressWarnings({"all"})
    public void tab(CommandAutoCompleteInteractionEvent event) {
        event.replyChoiceLongs(LongStream.range(1L, getMaxPages(event.getGuild())+1L).boxed().toList()).queue();
    }
}
