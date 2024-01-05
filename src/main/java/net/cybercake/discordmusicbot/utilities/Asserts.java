package net.cybercake.discordmusicbot.utilities;

import java.util.function.Supplier;

public class Asserts {

    public static boolean throwsError(Executable executable, Class<? extends Throwable> exceptionType) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            if(throwable.getClass().equals(exceptionType)) return true;
        }
        return false;
    }

    public static <T> T doesNotThrow(Class<T> returned, Supplier<T> executable, T def) {
        try {
            T obj = executable.get();
            if (obj == null) return def;
            return obj;
        } catch (Exception exception) {
            return def;
        }
    }

}
