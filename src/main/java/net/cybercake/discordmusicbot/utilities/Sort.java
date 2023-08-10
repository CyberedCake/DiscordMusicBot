package net.cybercake.discordmusicbot.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Sort {

    @SuppressWarnings({"all"})
    public static <T> List<T> reverseOrder(@NotNull List<T> list) {
        return list
                .stream()
                .collect(ArrayList::new, (l1, e) -> l1.add(0, e), (list1, list2) -> list1.addAll(0, list2))
                .stream()
                .map(object -> (T)object)
                .toList();
    }

}
