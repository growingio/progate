package io.growing.gateway.graphql.function;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void apply(T t, U u, V v);

}
