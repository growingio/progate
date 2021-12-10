package io.growing.progate;


import io.growing.gateway.cluster.ClusterStateException;
import io.growing.gateway.exception.PluginNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    private static final String ERROR_MESSAGE = "error";

    @Test
    void testConstructClusterStateException() {
        final ClusterStateException e = new ClusterStateException(ERROR_MESSAGE);
        Assertions.assertEquals(ERROR_MESSAGE, e.getMessage());
    }

    @Test
    void testConstructPluginNotFoundException() {
        final String plugin = "grpc";
        final PluginNotFoundException e = new PluginNotFoundException("grpc");
        Assertions.assertEquals(String.format("Plugin: %s cannot found.", plugin), e.getMessage());
    }

}
