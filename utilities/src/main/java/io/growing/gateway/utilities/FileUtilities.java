package io.growing.gateway.utilities;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author AI
 */
public final class FileUtilities {
    private FileUtilities() {
    }

    public static Set<Path> listAllFiles(final Path directory) throws IOException {
        final Set<Path> directories = Sets.newHashSet();
        directories.add(directory);
        return listAllFiles(directories);
    }

    public static Set<Path> listAllFiles(final Set<Path> directories) throws IOException {
        if (CollectionUtilities.isEmpty(directories)) {
            return Collections.emptySet();
        }
        final Set<Path> paths = new HashSet<>();
        final FileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file)) {
                    paths.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        for (Path directory : directories) {
            Files.walkFileTree(directory, visitor);
        }
        return paths;
    }

}
