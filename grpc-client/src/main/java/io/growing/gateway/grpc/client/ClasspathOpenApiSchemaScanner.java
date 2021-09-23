package io.growing.gateway.grpc.client;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.protobuf.ByteString;
import io.growing.gateway.FileDescriptorDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class ClasspathOpenApiSchemaScanner {

    public List<FileDescriptorDto> scan(final String root) throws IOException {
        final ImmutableSet<ClassPath.ResourceInfo> resourceInfos = ClassPath.from(this.getClass().getClassLoader()).getResources();
        final List<FileDescriptorDto> files = new LinkedList<>();
        for (ClassPath.ResourceInfo resourceInfo : resourceInfos) {
            final String name = resourceInfo.getResourceName();
            if (name.startsWith(root) && name.endsWith(".yaml")) {
                try (final InputStream is = resourceInfo.asByteSource().openStream()) {
                    final FileDescriptorDto descriptor = FileDescriptorDto.newBuilder().setName(name).setContent(ByteString.readFrom(is)).build();
                    files.add(descriptor);
                }
            }
        }
        return files;
    }

}
