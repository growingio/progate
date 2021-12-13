package io.growing.gateway.progate.exception;

import io.growing.gateway.graphql.exception.FetcherNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExceptionTest {
    @Test
    void testConstructFetcherNotFoundException() {
        final FetcherNotFoundException e = new FetcherNotFoundException();
        Assertions.assertEquals("Cannot found fetcher", e.getMessage());
    }

}
