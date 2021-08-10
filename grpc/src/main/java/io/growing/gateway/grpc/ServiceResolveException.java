package io.growing.gateway.grpc;

/**
 * @author AI
 */
public class ServiceResolveException extends RuntimeException{

    public ServiceResolveException(String message) {
        super(message);
    }

    public ServiceResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
