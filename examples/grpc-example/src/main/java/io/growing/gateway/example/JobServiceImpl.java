package io.growing.gateway.example;

import com.google.common.collect.Sets;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class JobServiceImpl extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void getJobs(GetJobsRequest request, StreamObserver<JobDto> responseObserver) {
        for (int i = 0; i < 100; i++) {
            final JobDto.Builder builder = JobDto.newBuilder();
            builder.setIndex(Int32Value.newBuilder().setValue(0).build());
            builder.setName("Hello: " + i);
            if (i % 2 == 0) {
                builder.setDescription(StringValue.of("hello example"));
            }
            builder.addAllTags(Sets.newHashSet("new", "gateway"));
            responseObserver.onNext(builder.build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createJob(CreateJobRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName(request.getJob().getName()).setDescription(request.getJob().getDescription()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

}
