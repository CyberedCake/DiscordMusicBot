package net.cybercake.discordmusicbot.generalutils;

public class Asserts {

    public static boolean throwsError(Executable executable, Class<? extends Throwable> exceptionType) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            if(throwable.getClass().equals(exceptionType)) return true;
        }
        return false;
    }

}
