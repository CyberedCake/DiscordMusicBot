package net.cybercake.discordmusicbot.generalutils;

public class Preconditions {

    public static void checkArgument(boolean check, String msg) {
        if(check) return;
        throw new IllegalArgumentException(msg);
    }

}
