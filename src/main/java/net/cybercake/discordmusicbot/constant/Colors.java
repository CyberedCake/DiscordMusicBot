package net.cybercake.discordmusicbot.constant;

import java.awt.*;

public enum Colors {

    SUCCESS(new Color(0, 211, 16)),
    LYRICS(new Color(186, 151, 255)),
    DISCONNECTED(new Color(255, 152, 68)),
    NOW_PLAYING(new Color(108, 221, 255)),
    SKIP(new Color(231, 255, 60)),
    PREVIOUS(new Color(255, 226, 60)),
    CURRENT_SONG_STATUS(new Color(179, 85, 255)),
    LIST(new Color(43, 255, 180)),
    QUEUE(new Color(0, 65, 59)),
    SHUTDOWN_FEEDBACK(new Color(62, 137, 255)),
    ERROR(new Color(186, 24, 19)),
    CONTACT(new Color(253, 204, 45));


    private final Color color;

    Colors(Color color) {
        this.color = color;
    }

    public Color get() { return this.color; }

}
