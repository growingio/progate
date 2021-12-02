package io.growing.progate.utilities;

import java.util.Collection;
import java.util.Objects;

/**
 * @author AI
 */
public final class CollectionUtilities {

    private CollectionUtilities() {
    }

    public static boolean isNotEmpty(final Collection<?> source) {
        return !isEmpty(source);
    }

    public static boolean isEmpty(final Collection<?> source) {
        return Objects.isNull(source) || source.isEmpty();
    }

}
