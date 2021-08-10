package io.growing.gateway.grpc.impl;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.growing.gateway.grpc.ServiceResolveException;
import io.growing.gateway.grpc.ServiceResolver;
import io.growing.gateway.grpc.marshaller.DynamicMessageMarshaller;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author AI
 */
public class FileDescriptorServiceResolver implements ServiceResolver {

    private final Map<String, Descriptors.FileDescriptor> fileDescriptors;

    public static FileDescriptorServiceResolver fromFileDescriptorProtoSet(final Set<DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet) {
        final Map<String, DescriptorProtos.FileDescriptorProto> fileDescriptorProtoMap = new HashMap<>();
        for (DescriptorProtos.FileDescriptorProto proto : fileDescriptorProtoSet) {
            fileDescriptorProtoMap.put(proto.getName(), proto);
        }
        return new FileDescriptorServiceResolver(fileDescriptorProtoMap);
    }

    public FileDescriptorServiceResolver(final Map<String, DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet) {
        this.fileDescriptors = new HashMap<>(fileDescriptorProtoSet.size());
        try {
            for (Map.Entry<String, DescriptorProtos.FileDescriptorProto> entry : fileDescriptorProtoSet.entrySet()) {
                final Descriptors.FileDescriptor fileDescriptor = buildFormProto(entry.getValue(), fileDescriptorProtoSet, fileDescriptors);
                fileDescriptors.put(fileDescriptor.getName(), fileDescriptor);
            }
        } catch (Descriptors.DescriptorValidationException e) {
            throw new ServiceResolveException("Cannot build service descriptor: " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Descriptors.MethodDescriptor getMethodDescriptor(String fullServiceName, String methodName) {
        final Optional<Descriptors.ServiceDescriptor> serviceDescriptorOpt = findService(fullServiceName);
        if (serviceDescriptorOpt.isEmpty()) {
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

    @Override
    public MethodDescriptor<DynamicMessage, DynamicMessage> resolveMethod(String fullServiceName, String methodName) {
        final Descriptors.MethodDescriptor methodDescriptor = getMethodDescriptor(fullServiceName, methodName);
        return resolveMethod(methodDescriptor);
    }

    private Optional<Descriptors.ServiceDescriptor> findService(final String fullServiceName) {
        final int splitIndex = fullServiceName.lastIndexOf('.');
        final String packageName = fullServiceName.substring(0, splitIndex);
        final String serviceName = fullServiceName.substring(splitIndex + 1);
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
        final String name = proto.getName();
        if (fileDescriptors.containsKey(name)) {
            return fileDescriptors.get(name);
        }
        final ImmutableList.Builder<Descriptors.FileDescriptor> dependencies = ImmutableList.builder();
        for (String dependencyName : proto.getDependencyList()) {
            final DescriptorProtos.FileDescriptorProto dependencyProto = fileDescriptorProtoSet.get(dependencyName);
            dependencies.add(buildFormProto(dependencyProto, fileDescriptorProtoSet, fileDescriptors));
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

}
