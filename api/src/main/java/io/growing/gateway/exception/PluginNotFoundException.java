package io.growing.gateway.exception;

public class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException(String name) {
        super(String.format("Plugin: %s cannot found.", name));
    }

}
