package webit.generator.util;

import java.util.Map;

/**
 *
 * @author Zqq
 */
public class Maps {

    public static interface Handler<K, V> {

        boolean each(K key, V value);
    }

    public static <K, V> boolean each(Map<K, V> map, Handler<K, V> handler) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (!handler.each(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
