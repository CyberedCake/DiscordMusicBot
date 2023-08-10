package net.cybercake.discordmusicbot.utilities;

import javax.annotation.CheckForNull;

public class Preconditions {

    public static void checkArgument(boolean check, String msg) {
        if(check) return;
        throw new IllegalArgumentException(msg);
    }

    public static <T> void checkNotNull(@CheckForNull T reference, String msg) {
        if(reference != null) return;
        throw new NullPointerException(msg);
    }

    public static boolean checkThrows(Executable executable, Class<? extends Throwable> checkThrows) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            return throwable.getClass().equals(checkThrows);
        }
        return false;
    }

}
