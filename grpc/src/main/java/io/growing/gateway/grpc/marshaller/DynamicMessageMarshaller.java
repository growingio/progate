package io.growing.gateway.grpc.marshaller;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author AI
 */
public class DynamicMessageMarshaller implements MethodDescriptor.Marshaller<DynamicMessage> {

    private final Descriptor descriptor;

    public DynamicMessageMarshaller(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public InputStream stream(DynamicMessage value) {
        return value.toByteString().newInput();
    }

    @Override
    public DynamicMessage parse(InputStream stream) {
        try {
            return DynamicMessage.newBuilder(descriptor).mergeFrom(stream).build();
        } catch (IOException e) {
            throw new MessageMarshallException(e);
        }
    }

}
