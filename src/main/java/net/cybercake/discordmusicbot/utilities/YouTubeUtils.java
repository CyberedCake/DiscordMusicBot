package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class YouTubeUtils {

    public static boolean isYouTube(AudioTrackInfo info) {
        return info.uri.contains("youtube.com") || info.uri.contains("youtu.be");
    }

    public static String getArtwork(AudioTrackInfo info){
        return "https://img.youtube.com/vi/" + info.identifier + "/maxresdefault.jpg";
    }

}
