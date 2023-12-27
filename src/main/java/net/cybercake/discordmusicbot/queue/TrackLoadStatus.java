package net.cybercake.discordmusicbot.queue;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cybercake.discordmusicbot.constant.Colors;
import net.cybercake.discordmusicbot.utilities.Log;
import net.cybercake.discordmusicbot.utilities.TrackUtils;
import net.cybercake.discordmusicbot.utilities.YouTubeUtils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class TrackLoadStatus implements com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler {

    private final MusicPlayer player;
    private final TrackLoadSettings settings;

    TrackLoadStatus(MusicPlayer player, TrackLoadSettings settings) {
        this.player = player;
        this.settings = settings;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.player.play(track, settings.shouldStartNow());

        track.setUserData(settings.getRequestedBy());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(track.getInfo().artworkUrl);
        builder.setColor(Colors.SUCCESS.get());
        if (settings.shouldStartNow())
            builder.setDescription("*This track will start after the current one finishes.*");
        builder.addField("Enqueued Track:", (settings.shouldStartNow() ? "`#" + (player.getTrackScheduler().getQueue().getCurrentIndex() + 1) + "`" : "`#" + player.getTrackScheduler().queue.getLiteralQueue().size() + "`") + " - [" + track.getInfo().title + "](" + TrackUtils.getUrlOf(track.getInfo()) + ")", true);
        builder.addField("Requested By:", settings.getRequestedBy().getAsMention(), true);
        builder.addField("Duration:", "`" + TrackUtils.getFormattedDuration(track.getDuration()) + "`", true);

        settings.getParentEvent().getHook().editOriginalEmbeds(builder.build()).queue();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if(playlist.isSearchResult()) {
            trackLoaded(playlist.getTracks().get(0));
            return;
        }

        List<AudioTrack> tracks = playlist.getTracks();
        if(settings.shouldShuffle())
            Collections.shuffle(tracks);
        tracks.forEach(track -> {
            track.setUserData(this.settings.getRequestedBy());
            this.player.play(track, this.settings.shouldStartNow());
        });

        EmbedBuilder builder = new EmbedBuilder();
        builder.setThumbnail(tracks.get(0).getInfo().artworkUrl);
        if(settings.shouldShuffle())
            builder.setDescription("*Added playlist tracks in a random order.*");
        builder.setColor(Colors.SUCCESS.get());
        builder.addField("Enqueued Playlist:", playlist.getName(), true);
        builder.addField("Requested By:", settings.getRequestedBy().getAsMention(), true);
        builder.addField("Items in Playlist:", "`" + playlist.getTracks().size() + "`", true);

        settings.getParentEvent().getHook().editOriginalEmbeds(builder.build()).queue();
    }

    @Override
    public void noMatches() {
        if(settings.getTrackUrl().equalsIgnoreCase(MusicPlayer.FILE_IDENTIFIER) && settings.getFile() != null) {
            try {
                InputStream input = settings.getFile().getProxy().download().join();
                AudioTrack decodedTrack = ((DefaultAudioPlayerManager) this.player.audioPlayerManager).decodeTrack(new MessageInput(input)).decodedTrack;
                Log.warn(decodedTrack.getInfo().title + " at " + decodedTrack.getInfo().isStream + " with " + decodedTrack.getInfo().length);
                this.trackLoaded(decodedTrack);
                input.close();
            } catch (Exception exception) {
                Log.error("Failed to load track from file.", exception);
                this.loadFailed(new FriendlyException("Failed to load that track from your file.", FriendlyException.Severity.COMMON, exception));
            }
            return;
        }

        settings.getParentEvent().getHook().editOriginal("Failed to find any track named `" + settings.getTrackUrl().replace("ytsearch:", "") + "`").queue();
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        settings.getParentEvent().getHook().editOriginal("Could not play that track. Try again later!\n\n`" + exception.getMessage() + "`" + (exception.getCause() != null ? " caused by `" + exception.getCause().getMessage() + "`" : "")).queue();
    }

}
