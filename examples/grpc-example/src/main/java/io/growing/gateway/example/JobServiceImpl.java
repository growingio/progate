package io.growing.gateway.example;

import com.google.common.collect.Sets;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class JobServiceImpl extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void getJobs(GetJobsRequest request, StreamObserver<JobDto> responseObserver) {
        System.out.println("invoke getJobs method");
        System.out.println(request.getIdsList());
        for (int i = 0; i < 100; i++) {
            final JobDto.Builder builder = JobDto.newBuilder();
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
        System.out.println("invoke createJob method");
        System.out.println(request.getType());
        final JobDto job = JobDto.newBuilder().setName(request.getJob().getName()).setDescription(request.getJob().getDescription()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

}
