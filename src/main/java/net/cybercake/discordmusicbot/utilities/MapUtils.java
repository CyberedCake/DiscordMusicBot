package net.cybercake.discordmusicbot.utilities;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    public static <K, V> Pair<V, Map<K,V>> getOrAddDefault(Map<K, V> parentMap, K key, V def) {
        Map<K, V> map = null;
        V value = parentMap.get(key);

        if (value == null) {
            value = def;
            map = new HashMap<>(parentMap);
            map.put(key, def);
        }

        return new Pair<>(value, map);
    }

}
