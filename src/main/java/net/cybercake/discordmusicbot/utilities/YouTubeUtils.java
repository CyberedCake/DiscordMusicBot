package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import javax.annotation.Nullable;

public class YouTubeUtils {

    public static boolean isYouTube(AudioTrackInfo info) {
        return info.uri.contains("youtube.com") || info.uri.contains("youtu.be");
    }

    public static String getArtwork(AudioTrackInfo info){
        return "https://img.youtube.com/vi/" + info.identifier + "/maxresdefault.jpg";
    }

}
