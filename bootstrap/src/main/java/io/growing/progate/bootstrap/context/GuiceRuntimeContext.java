package io.growing.progate.bootstrap.context;

import com.google.inject.Injector;
import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.meta.Upstream;

public class GuiceRuntimeContext implements RuntimeContext {
    private final String configPath;
    private final Injector injector;

    private GuiceRuntimeContext(String configPath, Injector injector) {
        this.configPath = configPath;
        this.injector = injector;
    }

    public static GuiceRuntimeContext from(final String configPath, final Injector injector) {
        return new GuiceRuntimeContext(configPath, injector);
    }

    @Override
    public String getConfigPath() {
        return configPath;
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return null;
    }

    @Override
    public Upstream getInternalUpstream(String name) {
        return null;
    }

}
