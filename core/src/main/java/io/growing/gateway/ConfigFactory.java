package io.growing.gateway;

public interface ConfigFactory {

    <T> T load(Class<T> clazz);

}
