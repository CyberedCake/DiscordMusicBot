package net.cybercake.discordmusicbot.queue.seek;

import net.cybercake.discordmusicbot.constant.Colors;

public enum SeekType {

    PREVIOUS(-1,
            Colors.SKIP,
            "go to the previous song",
            "Previous Song Vote",
            "wants to go back to <previous-song> instead of ",
            "going back to the previous song"
    ),
    NEXT(1,
            Colors.SKIP,
            "skip",
            "Skip Song Vote",
            "wants to skip",
            "skipping this song"
    );

    private final int intRep;
    private final Colors color;

    final String semantics;
    final String title;
    final String subtitle;
    final String execution;

    SeekType(int intRep, Colors colors, String semantics, String title, String subtitle, String execution) {
        this.intRep = intRep;
        this.color = colors;
        this.semantics = semantics;
        this.title = title;
        this.subtitle = subtitle;
        this.execution = execution;
    }

    public Colors getColor() { return this.color; }
    public int getIntRep() { return this.intRep; }

}
