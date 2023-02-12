package net.cybercake.discordmusicbot.generalutils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TrackUtils {

    public static String getDuration(long current, long max) {
        StringBuilder bar = new StringBuilder();
        float currentLoop = 0;
        while (currentLoop < 1) {
            if(Math.abs(currentLoop-(Float.parseFloat(String.valueOf(current/max)))) <= 0.1)
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

}
