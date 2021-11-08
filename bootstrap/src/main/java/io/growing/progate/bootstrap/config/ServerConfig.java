package io.growing.progate.bootstrap.config;

import java.util.List;

public class ServerConfig {
    private String host;
    private Integer port;
    private List<ConfigEntry> env;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<ConfigEntry> getEnv() {
        return env;
    }

    public void setEnv(List<ConfigEntry> env) {
        this.env = env;
    }
}
