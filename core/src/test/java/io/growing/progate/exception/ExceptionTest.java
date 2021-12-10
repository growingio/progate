package io.growing.progate.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExceptionTest {

    @Test
    void testConstructConfigParseException() {
        final Throwable cause = new NullPointerException();
        final String message = "error";
        final ConfigParseException e = new ConfigParseException(message, cause);
        Assertions.assertEquals(message, e.getMessage());
        Assertions.assertEquals(cause, e.getCause());
    }
}
