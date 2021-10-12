package io.growing.gateway.grpc.client;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.protobuf.ByteString;
import io.growing.gateway.FileDescriptorDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClasspathGraphqlSchemaScanner {

    public List<FileDescriptorDto> scan(final String root) throws IOException {
        final ImmutableSet<ClassPath.ResourceInfo> resourceInfos = ClassPath.from(this.getClass().getClassLoader()).getResources();
        final List<FileDescriptorDto> files = new LinkedList<>();
        for (ClassPath.ResourceInfo resourceInfo : resourceInfos) {
            final String name = resourceInfo.getResourceName();
            if (name.startsWith(root) && name.endsWith(".graphql")) {
                try (final InputStream is = resourceInfo.asByteSource().openStream()) {
                    final FileDescriptorDto descriptor = FileDescriptorDto.newBuilder().setName(name).setContent(ByteString.readFrom(is)).build();
                    files.add(descriptor);
                }

            }
        }
        return files;
    }

    public List<FileDescriptorDto> scan(ClassLoader[] classLoaders, final String root) throws IOException {
        Set<ClassPath.ResourceInfo> resourceInfos = Sets.newHashSet();
        List<FileDescriptorDto> files = new LinkedList<>();
        Arrays.asList(classLoaders).forEach(classLoader -> {
            try {
                resourceInfos.addAll(ClassPath.from(classLoader).getResources());
            } catch (IOException e) {
                // 日志处理
            }
        });
        for (ClassPath.ResourceInfo resourceInfo : resourceInfos) {
            final String name = resourceInfo.getResourceName();
            if (name.startsWith(root) && name.endsWith(".graphql")) {
                try (final InputStream is = resourceInfo.asByteSource().openStream()) {
                    final FileDescriptorDto descriptor = FileDescriptorDto.newBuilder().setName(name).setContent(ByteString.readFrom(is)).build();
                    files.add(descriptor);
                }

            }
        }
        return files;
    }

}
