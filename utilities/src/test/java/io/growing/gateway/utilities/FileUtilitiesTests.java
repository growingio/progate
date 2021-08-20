package io.growing.gateway.utilities;

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
public class FileUtilitiesTests {

    @Test
    public void testListFiles() throws IOException {
        final Set<Path> paths = FileUtilities.listAllFiles(Paths.get(SystemUtils.getUserDir().getAbsolutePath()));
        try (Stream<Path> stream = paths.stream()) {
            Assertions.assertTrue(stream.anyMatch(path -> "FileUtilities.java".equals(path.getFileName().toString())));
        }
    }

    @Test
    public void testEmpty() throws IOException {
        final Set<Path> paths = FileUtilities.listAllFiles(Sets.newHashSet());
        Assertions.assertTrue(paths.isEmpty());
    }

}
