package io.growing.gateway.example;

import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class JobServiceImpl extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void getJobs(GetJobsRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName("Hello").build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

    @Override
    public void createJob(CreateJobRequest request, StreamObserver<JobDto> responseObserver) {
        final JobDto job = JobDto.newBuilder().setName(request.getJob().getName()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

}