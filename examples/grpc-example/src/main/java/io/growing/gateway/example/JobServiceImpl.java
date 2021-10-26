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
    public void listJobs(ListJobsRequest request, StreamObserver<ListJobsResponse> responseObserver) {
        final ListJobsResponse.Builder responseBuilder = ListJobsResponse.newBuilder();
        for (int i = 0; i < 100; i++) {
            final JobDto.Builder builder = JobDto.newBuilder();
            builder.setIndex(Int32Value.newBuilder().setValue(0).build());
            builder.setName("Hello: " + i);
            if (i % 2 == 0) {
                builder.setDescription(StringValue.of("hello example"));
            }
            builder.addAllTags(Sets.newHashSet("new", "gateway"));
            responseBuilder.addBooks(builder.build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getJob(GetJobRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName("获取任务").setDescription(StringValue.newBuilder().setValue("这个是新的任务").build()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

    @Override
    public void createJob(CreateJobRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName(request.getJob().getName()).setDescription(request.getJob().getDescription()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

}
