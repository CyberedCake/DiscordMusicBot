package net.cybercake.discordmusicbot.commands.list.user;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.queue.TrackLoadSettings;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.annotation.Nullable;
import java.util.Objects;

public class PlayFile extends Command {

    public PlayFile() {
        super("playfile", "Plays a file instead of querying a database.");
        this.optionData = new OptionData[]{
                new OptionData(OptionType.ATTACHMENT, "attachment", "The file to play from.", true),
                new OptionData(OptionType.BOOLEAN, "priority", "Plays this song after the current song.", false),
                new OptionData(OptionType.BOOLEAN, "shuffle", "Shuffles the playlist.", false)
        };
        this.requireDjRole = true;
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        Message.Attachment file = Objects.requireNonNull(event.getOption("attachment")).getAsAttachment();
        OptionMapping priority = event.getOptions().stream().filter(option -> option.getName().equalsIgnoreCase("priority")).findFirst().orElse(null);
        OptionMapping shuffle = event.getOptions().stream().filter(option -> option.getName().equalsIgnoreCase("shuffle")).findFirst().orElse(null);
        this.handlePlayFile(event, file, (priority == null ? null : priority.getAsBoolean()), (shuffle== null ? null : shuffle.getAsBoolean()));
    }

    @SuppressWarnings("DuplicatedCode")
    public void handlePlayFile(SlashCommandInteractionEvent event, Message.Attachment file, @Nullable Boolean priority, @Nullable Boolean shuffle) {
        Member member = event.getMember();

        event.deferReply().queue();
        if(PresetExceptions.memberNull(event)) return;
        assert member != null;
        User user = member.getUser();

        if(member.getVoiceState() == null || member.getVoiceState().getChannel() == null) { // they are not in a voice chat
            Embeds.throwError(event, user, "You must be in a voice channel to continue.", true, null); return;
        }

        MusicPlayer musicPlayer = Main.musicPlayerManager.getGuildMusicPlayerForChannelsElseCreate(member.getGuild(), member.getVoiceState().getChannel(), event.getChannel().asTextChannel());
        if(musicPlayer != null && !member.getVoiceState().getChannel().equals(musicPlayer.getVoiceChannel())) {
            Embeds.throwError(event, user, "You must be in the voice channel " + musicPlayer.getVoiceChannel().getAsMention() + " to continue.", true, null); return;
        }

        try {
            Main.musicPlayerManager.getGuildMusicPlayer(member.getGuild()).loadAndPlay( // repeated because this should create a new one
                    TrackLoadSettings.config(
                                    user,
                                    file,
                                    event,
                                    this,
                                    priority != null && priority,
                                    shuffle != null && shuffle
                                    )
                            .build()
            );
        } catch (Exception exception) {
            Embeds.throwError(event, user, "A general error occurred whilst trying to add the song! `" + exception + "`", exception);
        }
    }

}
