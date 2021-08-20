package io.growing.gateway.example;

import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class JobServiceImpl extends JobServiceGrpc.JobServiceImplBase {

    @Override
    public void getJobs(GetJobsRequest request, StreamObserver<JobDto> responseObserver) {
        System.out.println("invoke getJobs method");
        for (int i = 0; i < 100; i++) {
            final JobDto job = JobDto.newBuilder().setName("Hello: " + i).build();
            responseObserver.onNext(job);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createJob(CreateJobRequest request, StreamObserver<JobDto> responseObserver) {
        System.out.println("invoke createJob method");
        final JobDto job = JobDto.newBuilder().setName(request.getJob().getName()).build();
        responseObserver.onNext(job);
        responseObserver.onCompleted();
    }

}
