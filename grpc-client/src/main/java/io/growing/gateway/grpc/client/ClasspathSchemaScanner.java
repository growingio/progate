package io.growing.gateway.grpc.client;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.protobuf.ByteString;
import io.growing.gateway.FileDescriptorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public enum ClasspathSchemaScanner {

    OPEN_API("OpenAPI", ".yaml"),
    GRAPHQL("GraphQL", ".graphql");

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathSchemaScanner.class);

    private final String name;
    private final String extension;

    ClasspathSchemaScanner(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public List<FileDescriptorDto> scan(final String root) throws IOException {
        return scan(root, this.getClass().getClassLoader());
    }

    public List<FileDescriptorDto> scan(final String root, ClassLoader... classLoaders) throws IOException {
        Arrays.stream(classLoaders).forEach(classLoader -> {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{} schema scan class loader：{}", name, classLoader);
            }
        });
        Set<ClassPath.ResourceInfo> resourceInfos = Sets.newHashSet();
        List<FileDescriptorDto> files = new LinkedList<>();
        Arrays.asList(classLoaders).forEach(classLoader -> {
            try {
                resourceInfos.addAll(ClassPath.from(classLoader).getResources());
            } catch (IOException e) {
                LOGGER.error(name + " schema load exception", e);
            }
        });
        final String fileSeparator = FileSystems.getDefault().getSeparator();
        final String prefix = root.endsWith(fileSeparator) ? root : root + fileSeparator;
        for (ClassPath.ResourceInfo resourceInfo : resourceInfos) {
            final String name = resourceInfo.getResourceName();
            if (name.startsWith(prefix) && name.endsWith(extension)) {
                try (final InputStream is = resourceInfo.asByteSource().openStream()) {
                    final FileDescriptorDto descriptor = FileDescriptorDto.newBuilder().setName(name).setContent(ByteString.readFrom(is)).build();
                    files.add(descriptor);
                }
            }
        }
        return files;
    }


}
