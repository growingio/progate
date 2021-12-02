package io.growing.progate.utilities;

import com.google.common.collect.Sets;
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
class FileUtilitiesTest {

    @Test
    void testListFiles() throws IOException {
        final Set<Path> paths = FileUtilities.listAllFiles(Paths.get(SystemUtils.getUserDir().getAbsolutePath()));
        try (Stream<Path> stream = paths.stream()) {
            Assertions.assertTrue(stream.anyMatch(path -> "FileUtilities.java".equals(path.getFileName().toString())));
        }
    }

    @Test
    void testEmpty() throws IOException {
        final Set<Path> paths = FileUtilities.listAllFiles(Sets.newHashSet());
        Assertions.assertTrue(paths.isEmpty());
    }

}
