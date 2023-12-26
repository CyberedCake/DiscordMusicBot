package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.cybercake.discordmusicbot.Main;
import org.jetbrains.annotations.Nullable;

public class SpotifyUtils {

    public static String extractImage(AudioTrackInfo info) {
        @Nullable String image = null;
        if (info.uri.contains("spotify.com")) {
            Log.info("Finding spotify.com extractImage");
            try {
                image = Main.spotifyApi.getTrack(info.identifier).build().execute().getAlbum().getImages()[0].getUrl();
                Log.info("Found: " + image);
            } catch (Exception exception) {
                Log.error("Errored: " + exception);
            }
        }
        return image;
    }

}
