package io.growing.gateway.grpc.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.growing.gateway.grpc.ServiceResolveException;
import io.growing.gateway.grpc.ServiceResolver;
import io.growing.gateway.grpc.marshaller.DynamicMessageMarshaller;
import io.grpc.MethodDescriptor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author AI
 */
public class FileDescriptorServiceResolver implements ServiceResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDescriptorServiceResolver.class);
    private final Set<Descriptors.Descriptor> typeDescriptors;
    private final Map<String, Descriptors.FileDescriptor> fileDescriptors;

    public static FileDescriptorServiceResolver fromFileDescriptorProtoSet(final List<DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet) {
        final Map<String, DescriptorProtos.FileDescriptorProto> fileDescriptorProtoMap = new HashMap<>();
        LOGGER.info("Find proto size: {}", fileDescriptorProtoSet.size());
        for (DescriptorProtos.FileDescriptorProto proto : fileDescriptorProtoSet) {
            LOGGER.info("Find proto file: {}", proto.getName());
            final String key = proto.getPackage() + "::" + proto.getName();
            if (!fileDescriptorProtoMap.containsKey(key)) {
                fileDescriptorProtoMap.put(key, proto);
            }
        }
        LOGGER.info("Mapped proto size: {}", fileDescriptorProtoMap.size());
        return new FileDescriptorServiceResolver(fileDescriptorProtoMap);
    }

    public FileDescriptorServiceResolver(final Map<String, DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet) {
        this.fileDescriptors = new HashMap<>(fileDescriptorProtoSet.size());
        final ImmutableSet.Builder<Descriptors.Descriptor> typeSet = ImmutableSet.builder();
        try {
            for (Map.Entry<String, DescriptorProtos.FileDescriptorProto> entry : fileDescriptorProtoSet.entrySet()) {
                final Descriptors.FileDescriptor fileDescriptor = buildFormProto(entry.getValue(), fileDescriptorProtoSet, fileDescriptors);
                typeSet.addAll(fileDescriptor.getMessageTypes());
                final String key = fileDescriptor.getPackage() + "::" + fileDescriptor.getName();
                fileDescriptors.put(key, fileDescriptor);
            }
            typeDescriptors = typeSet.build();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new ServiceResolveException("Cannot build service descriptor: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Set<Descriptors.Descriptor> getTypeDescriptors() {
        return typeDescriptors;
    }

    @Override
    public Descriptors.MethodDescriptor getMethodDescriptor(String endpoint) {
        final Pair<String, String> part = partition(endpoint, '/');
        final String fullServiceName = part.getLeft();
        final String methodName = part.getRight();
        final Optional<Descriptors.ServiceDescriptor> serviceDescriptorOpt = findService(fullServiceName);
        if (!serviceDescriptorOpt.isPresent()) {
            throw new ServiceResolveException("Cannot found service: " + fullServiceName);
        }
        final String fullMethodName = io.grpc.MethodDescriptor.generateFullMethodName(fullServiceName, methodName);
        final Descriptors.MethodDescriptor methodDescriptor = serviceDescriptorOpt.get().findMethodByName(methodName);
        if (Objects.isNull(methodDescriptor)) {
            throw new ServiceResolveException("Cannot found method: " + fullMethodName);
        }
        return methodDescriptor;
    }

    @Override
    public MethodDescriptor<DynamicMessage, DynamicMessage> resolveMethod(Descriptors.MethodDescriptor descriptor) {
        final String fullProtoMethodName = descriptor.getFullName();
        final int splitIndex = fullProtoMethodName.lastIndexOf('.');
        final String fullServiceName = fullProtoMethodName.substring(0, splitIndex);
        final String methodName = fullProtoMethodName.substring(splitIndex + 1);
        final MethodDescriptor.MethodType methodType = getGrpcMethodType(descriptor);
        final String fullMethodName = io.grpc.MethodDescriptor.generateFullMethodName(fullServiceName, methodName);
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
            .setFullMethodName(fullMethodName).setType(methodType)
            .setRequestMarshaller(new DynamicMessageMarshaller(descriptor.getInputType()))
            .setResponseMarshaller(new DynamicMessageMarshaller(descriptor.getOutputType())).build();
    }

    private Optional<Descriptors.ServiceDescriptor> findService(final String fullServiceName) {
        final Pair<String, String> part = partition(fullServiceName, '.');
        final String packageName = part.getLeft();
        final String serviceName = part.getRight();
        Descriptors.ServiceDescriptor serviceDescriptor = null;
        for (Map.Entry<String, Descriptors.FileDescriptor> entry : fileDescriptors.entrySet()) {
            final Descriptors.FileDescriptor fileDescriptor = entry.getValue();
            if (packageName.equals(fileDescriptor.getPackage())) {
                serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
                if (Objects.nonNull(serviceDescriptor)) {
                    break;
                }
            }
        }
        return Optional.ofNullable(serviceDescriptor);
    }

    private Descriptors.FileDescriptor buildFormProto(final DescriptorProtos.FileDescriptorProto proto,
                                                      final Map<String, DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet,
                                                      final Map<String, Descriptors.FileDescriptor> fileDescriptors) throws Descriptors.DescriptorValidationException {
        final String key = proto.getPackage() + "::" + proto.getName();
        if (fileDescriptors.containsKey(key)) {
            return fileDescriptors.get(key);
        }
        final ImmutableList.Builder<Descriptors.FileDescriptor> dependencies = ImmutableList.builder();
        for (String dependencyName : proto.getDependencyList()) {
            for (Map.Entry<String, DescriptorProtos.FileDescriptorProto> entry : fileDescriptorProtoSet.entrySet()) {
                if (dependencyName.equals(entry.getValue().getName())) {
                    final String dependencyKey = entry.getValue().getPackage() + "::" + entry.getValue().getName();
                    final DescriptorProtos.FileDescriptorProto dependencyProto = fileDescriptorProtoSet.get(dependencyKey);
                    dependencies.add(buildFormProto(dependencyProto, fileDescriptorProtoSet, fileDescriptors));
                }
            }
        }
        return Descriptors.FileDescriptor.buildFrom(proto, dependencies.build().toArray(new Descriptors.FileDescriptor[0]));
    }

    private MethodDescriptor.MethodType getGrpcMethodType(final Descriptors.MethodDescriptor methodDescriptor) {
        if (methodDescriptor.isClientStreaming() && methodDescriptor.isServerStreaming()) {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        } else if (methodDescriptor.isClientStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else if (methodDescriptor.isServerStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else {
            return MethodDescriptor.MethodType.UNARY;
        }
    }

    private Pair<String, String> partition(final String str, final char separator) {
        final int index = str.lastIndexOf(separator);
        if (index < 0) {
            throw new IllegalArgumentException(str + " cannot be partition");
        }
        final String l = str.substring(0, index);
        final String r = str.substring(index + 1);
        return Pair.of(l, r);
    }

}
