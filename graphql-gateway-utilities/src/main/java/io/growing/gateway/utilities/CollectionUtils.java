package io.growing.gateway.utilities;

import java.util.Collection;
import java.util.Objects;

/**
 * @author AI
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isNotEmpty(final Collection<?> source) {
        return !isEmpty(source);
    }

    public static boolean isEmpty(final Collection<?> source) {
        return Objects.isNull(source) || source.isEmpty();
    }

}
