package io.growing.gateway.pipeline;

public interface ServiceInterceptor {

    void access(Request request, Response response);

    void responseFilter(Request request, Response response);

    void log(Request request, Response response);

}
