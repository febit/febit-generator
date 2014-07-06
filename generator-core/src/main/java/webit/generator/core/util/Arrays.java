package webit.generator.core.util;

import java.util.Collection;

/**
 *
 * @author Zqq
 */
public class Arrays {

    public static interface Handler<V> {

        boolean each(int index, V value);
    }

    public static <V> boolean each(Collection<V> collection, Handler<V> handler) {
        int index = 0;
        for (V item : collection) {
            if (handler.each(index++, item) == false) {
                return false;
            }
        }
        return true;
    }
}
