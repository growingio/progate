package io.growing.gateway.context;

import io.growing.gateway.exception.PluginNotFoundException;
import io.growing.gateway.meta.Upstream;

public interface RuntimeContext {

    String getConfigPath();

    <T> T getInstance(Class<T> clazz);

    Upstream getInternalUpstream(String name);

    <T> T createPlugin(String className) throws PluginNotFoundException;

}
