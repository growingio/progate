package io.growing.gateway.grpc.observer;

import io.grpc.stub.StreamObserver;
import io.vertx.core.Promise;

import java.util.concurrent.CompletionStage;


/**
 * @author AI
 */
public class UnaryObserver<V> implements StreamObserver<V> {

    private final Promise<V> promise = Promise.promise();
    private V value;

    @Override
    public void onNext(V value) {
        this.value = value;
    }

    @Override
    public void onError(Throwable t) {
        promise.fail(t);
    }

    @Override
    public void onCompleted() {
        promise.complete(value);
    }

    public CompletionStage<V> toCompletionStage() {
        return promise.future().toCompletionStage();
    }

}
