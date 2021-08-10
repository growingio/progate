package io.growing.gateway.utilities;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class FileUtilsTests {

    @Test
    public void testListFiles() throws IOException {
        final Set<Path> paths = FileUtils.listAllFiles(Paths.get(SystemUtils.getUserDir().getAbsolutePath()));
        try (Stream<Path> stream = paths.stream()) {
            Assertions.assertTrue(stream.anyMatch(path -> "FileUtils.java".equals(path.getFileName().toString())));
        }
    }

}
