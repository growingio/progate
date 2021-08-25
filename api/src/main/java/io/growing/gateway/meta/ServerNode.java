package io.growing.gateway.meta;

/**
 * @author AI
 */
public interface ServerNode {

    String id();

    String host();

    int port();

    int weight();

    boolean isAvailable();

}
