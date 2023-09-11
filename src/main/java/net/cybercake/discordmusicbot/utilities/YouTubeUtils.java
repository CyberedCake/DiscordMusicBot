package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import javax.annotation.Nullable;

public class YouTubeUtils {

    public static String extractImage(AudioTrackInfo info) {
        @Nullable String image = null;
        if(info.uri.contains("youtube.com"))
            image = "https://i3.ytimg.com/vi/" + info.identifier + "/maxresdefault.jpg";
        return image;
    }

    public static String getThumbnailLinkFor(AudioTrack track){
        return "https://img.youtube.com/vi/" + track.getIdentifier() + "/maxresdefault.jpg";
    }
}
