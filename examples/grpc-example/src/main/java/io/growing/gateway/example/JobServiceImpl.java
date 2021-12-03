package io.growing.gateway.example;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class JobServiceImpl extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void get(GetJobRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName("job" + request.getId()).setDescription(StringValue.newBuilder().setValue("First job").build()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

    @Override
    public void list(ListJobRequest request, StreamObserver<JobDto> responseObserver) {
        for (int i = 0; i < 100; i++) {
            final JobDto job = JobDto.newBuilder().setName("job" + i).setDescription(StringValue.newBuilder().setValue("First job").build()).build();
            responseObserver.onNext(job);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteJobRequest request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
