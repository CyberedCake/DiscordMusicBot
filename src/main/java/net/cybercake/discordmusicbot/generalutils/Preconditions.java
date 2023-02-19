package net.cybercake.discordmusicbot.generalutils;

public class Preconditions {

    public static void checkArgument(boolean check, String msg) {
        if(check) return;
        throw new IllegalArgumentException(msg);
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
