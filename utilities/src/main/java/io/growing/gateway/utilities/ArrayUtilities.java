package io.growing.gateway.utilities;

import java.util.function.Function;

public final class ArrayUtilities {
    private ArrayUtilities() {
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R[] map(final T[] array, final Function<T, R> mapper) {
        final Object[] results = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            results[i] = mapper.apply(array[i]);
        }
        return (R[]) results;
    }

}
