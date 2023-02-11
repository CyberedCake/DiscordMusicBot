package net.cybercake.discordmusicbot;

public enum PlayStatus {

    PLAYING,
    IDLE_IN_VC,
    INACTIVE;

    private static PlayStatus playStatus;

    public static void setStatus(PlayStatus playStatus) {
        PlayStatus.playStatus = playStatus;
    }
    public static PlayStatus getStatus() { return playStatus; }

}
