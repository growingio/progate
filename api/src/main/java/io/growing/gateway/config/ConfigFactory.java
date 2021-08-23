package io.growing.gateway.config;

public interface ConfigFactory {

    <T> T load(Class<T> clazz);

}
