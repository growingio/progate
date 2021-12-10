package io.growing.progate.config;

import io.growing.gateway.config.ConfigFactory;
import io.growing.progate.Resources;
import io.growing.progate.exception.ConfigParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

class YamlConfigFactoryTest {

    @Test
    void test() throws IOException {
        final Resources.Resource resource = Mockito.mock(Resources.Resource.class);
        Mockito.when(resource.utf8String()).thenThrow(new IOException());
        final ConfigFactory cf = new YamlConfigFactoryImpl(resource);
        Assertions.assertThrows(ConfigParseException.class, () -> cf.load(UpstreamConfig.class));
    }

}

