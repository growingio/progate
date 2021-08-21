package io.growing.gateway.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor;

import java.util.Set;

/**
 * @author AI
 */
public interface ServiceResolver {

    Set<Descriptors.Descriptor> getTypeDescriptors();

    Descriptors.MethodDescriptor getMethodDescriptor(String endpoint);

    MethodDescriptor<DynamicMessage, DynamicMessage> resolveMethod(Descriptors.MethodDescriptor descriptor);

}
