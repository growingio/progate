package io.growing.gateway.grpc;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor;

/**
 * @author AI
 */
public interface ServiceResolver {

    Descriptors.MethodDescriptor getMethodDescriptor(String fullServiceName, String methodName);

    MethodDescriptor<DynamicMessage, DynamicMessage> resolveMethod(Descriptors.MethodDescriptor descriptor);

    MethodDescriptor<DynamicMessage, DynamicMessage> resolveMethod(String fullServiceName, String methodName);

}
