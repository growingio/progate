package io.growing.progate.bootstrap.context;

import com.google.inject.Injector;
import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.exception.PluginNotFoundException;
import io.growing.gateway.meta.Upstream;

import java.util.List;
import java.util.Optional;

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
        final ClusterDiscoveryService discovery = injector.getInstance(ClusterDiscoveryService.class);
        final List<Upstream> upstreams = discovery.discover();
        final Optional<Upstream> upstreamOpt = upstreams.stream().filter(upstream -> upstream.isInternal() && upstream.name().equals(name)).findFirst();
        assert upstreamOpt.isPresent();
        return upstreamOpt.get();
    }

    @Override
    public <T> T createPlugin(String className) throws PluginNotFoundException {
        try {
            final Class clazz = Class.forName(className);
            return (T) injector.getInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new PluginNotFoundException(className);
        }
    }
}
