package io.growing.progate;

import io.growing.progate.resource.ClassPathResource;
import io.growing.progate.resource.URLResource;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

class ResourcesTest {
    @Test
    void testReadClassPathResource() throws IOException {
        final Resources.Resource resource = Resources.from("classpath:/main.conf");
        Assertions.assertTrue(resource instanceof ClassPathResource);
        Assertions.assertTrue(resource.bytes().length > 1);
        Assertions.assertTrue(resource.utf8String().length() > 1);
    }

    @Test
    void testReadNullClassPathResource() throws IOException {
        final Resources.Resource resource = Resources.from("classpath:/mian2.conf");
        Assertions.assertTrue(resource instanceof ClassPathResource);
        Assertions.assertEquals(0, resource.bytes().length);
    }

    @Test
    void testReadUrlResource() throws IOException {
        final Path path = Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "build.gradle.kts");
        final Resources.Resource resource = Resources.from("file://" + path.toAbsolutePath());
        Assertions.assertTrue(resource instanceof URLResource);
        Assertions.assertTrue(resource.bytes().length > 1);
        Assertions.assertTrue(resource.utf8String().length() > 1);
    }

}
