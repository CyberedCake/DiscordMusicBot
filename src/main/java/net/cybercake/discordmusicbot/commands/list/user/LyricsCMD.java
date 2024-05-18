package net.cybercake.discordmusicbot.commands.list.user;

import net.cybercake.discordmusicbot.Main;
import net.cybercake.discordmusicbot.PresetExceptions;
import net.cybercake.discordmusicbot.commands.Command;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.utilities.Embeds;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public class LyricsCMD extends Command {

    public LyricsCMD() {
        super(
                "lyrics", "Finds the lyrics to the current song or a search query."
        );
        this.optionData = new OptionData[]{new OptionData(OptionType.STRING, "song-title", "Search lyrics for a song title on A-Z Lyrics, Genius, or MusixMatch.", false)};
    }

    @Override
    public void command(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if(event.getOption("song-title") == null && !Main.musicPlayerManager.checkMusicPlayerExists(Objects.requireNonNull(event.getGuild()))) {
            PresetExceptions.trackIsNotPlaying(event, Objects.requireNonNull(event.getMember()), true);
            return;
        }

        String songTitle = (event.getOption("song-title") != null
                ? Objects.requireNonNull(event.getOption("song-title")).getAsString()
                : Main.musicPlayerManager.getGuildMusicPlayer(event.getGuild()).getAudioPlayer().getPlayingTrack().getInfo().title);

        EmbedBuilder builder = new EmbedBuilder();
        try {
            com.jagrosh.jlyrics.Lyrics lyrics = Main.lyricsClient.getLyrics(songTitle).get();
            if(lyrics == null) {
                builder = Embeds.getErrorEmbed(event.getUser(), "Cannot find a song with lyrics by that title (`" + songTitle + "`)");
                throw new NullPointerException("\"" + songTitle + "\" doesn't have lyrics associated with it");
            }
            builder.setTitle(lyrics.getTitle(), lyrics.getURL());

            StringBuilder trueLyrics = new StringBuilder();
            for (String lyric : lyrics.getContent().strip().split("\n")) {
                if (lyric.isBlank()) continue;
                trueLyrics.append(lyric).append("\n");
            }

            builder.setDescription(String.join(", ", trueLyrics));
            builder.setFooter("Source: " + lyrics.getSource() + "\n" + lyrics.getURL());
            builder.setColor(Colors.LYRICS.get());
        } catch (Exception exception) {
            if(!exception.getClass().equals(NullPointerException.class))
                builder = Embeds.getTechnicalErrorEmbed(event.getUser(), exception.toString());
        }
        event.getHook().editOriginalEmbeds(builder.build()).queue();
    }
}
