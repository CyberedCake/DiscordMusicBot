package net.cybercake.discordmusicbot.utilities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;

public class TrackUtils {

    public static String getDuration(long current, long max) {
        StringBuilder bar = new StringBuilder();
        float currentLoop = 0;
        while (currentLoop < 1) {
            if(Math.abs(currentLoop-((float)current/(float)max)+0.05) <= 0.05)
                bar.append(":radio_button:");
            else
                bar.append("▬");
            currentLoop += 0.1;
        }
        return getFormattedDuration(current) + " " + bar + " " + getFormattedDuration(max);
    }

    public static String getFormattedDuration(long duration) {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static Pair<User, Exception> deserializeUserData(Object object) {
        if(object instanceof User user) return new Pair<>(user, null);
        if(object instanceof Pair<?,?> pair) {
            if(!(pair.getFirstItem() instanceof User user)) throw new IllegalArgumentException("First item in Pair is not an instance of " + User.class.getCanonicalName());
            if(!(pair.getSecondItem() instanceof Exception exception)) throw new IllegalArgumentException("First item in Pair is not an instance of " + Exception.class.getCanonicalName());
            return new Pair<>(user, exception);
        }
        return null;
    }

    public static String getUrlOf(AudioTrackInfo info) {
        return getUrlOf(info, null);
    }

    public static String getUrlOf(AudioTrackInfo info, @Nullable String timing) {
        if (YouTubeUtils.isYouTube(info)) return "https://youtube.com/watch?v=" + info.identifier + (timing == null ? "" : timing);

        return info.uri;
    }

}
