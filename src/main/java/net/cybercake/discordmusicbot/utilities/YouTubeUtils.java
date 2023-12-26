package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import javax.annotation.Nullable;

public class YouTubeUtils {

    public static String extractImage(AudioTrackInfo info) {
        @Nullable String image = null;
        if(info.uri.contains("youtube.com"))
            image = "https://i3.ytimg.com/vi/" + info.identifier + "/mqdefault.jpg";
        return image;
    }

    public static boolean isYouTube(AudioTrackInfo info) {
        return info.uri.contains("youtube.com") || info.uri.contains("youtu.be");
    }

}
