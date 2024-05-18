package net.cybercake.discordmusicbot.commands.list.user;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.queue.MusicPlayer;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Remove extends Command {

    public Remove() {
        super("remove", "Removes a song from the queue given its position.");
        this.requireDjRole = true;
        this.optionData = new OptionData[]{
                new OptionData(OptionType.INTEGER, "position", "The position in the queue the song is. See /queue.", true, false)
        };
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        if(PresetExceptions.memberNull(event)) return;
        assert event.getMember() != null;

        MusicPlayer musicPlayer = PresetExceptions.trackIsNotPlaying(event, event.getMember(), true);
        if(musicPlayer == null) return;

        OptionMapping possibleOption = event.getOptions().stream().filter(option -> option.getName().equalsIgnoreCase("position")).findFirst().orElse(null);
        if(possibleOption == null) {
            Embeds.executeEmbed(event, Embeds.getTechnicalErrorEmbed(event.getUser(), "option not set, (required=true, found=null, invalid=true). try again later!"), true); return;
        }

        int position = possibleOption.getAsInt() - 1;

        if(position >= musicPlayer.getTrackScheduler().getQueue().getLiteralQueue().size()) {
            Embeds.throwError(event, event.getUser(), "You cannot remove a position that is not in the queue.", true, null); return;
        }

        if(position < 0){
            Embeds.throwError(event, event.getUser(), "You cannot remove a position less than zero.", true, null); return;
        }

        AudioTrack removed = musicPlayer.getTrackScheduler().remove(position);
        event.reply("Removed `" + removed.getInfo().title + "` from the queue in position `" + (position + 1) + "`").queue();
    }
}
