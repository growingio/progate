package io.growing.gateway.graphql.config;

import java.util.List;
import java.util.Map;

public class GraphqlConfig {
    private String path;
    private List<String> schemas;
    private List<String> plugins;
    private Map<String, Map<String, Object>> settings;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public Map<String, Map<String, Object>> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Map<String, Object>> settings) {
        this.settings = settings;
    }
}
