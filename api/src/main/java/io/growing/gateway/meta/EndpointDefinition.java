package io.growing.gateway.meta;

public class EndpointDefinition {
    private final String name;
    private final byte[] content;

    public EndpointDefinition(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

}
