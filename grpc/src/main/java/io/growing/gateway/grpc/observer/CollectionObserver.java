package io.growing.gateway.grpc.observer;

import io.grpc.stub.StreamObserver;
import io.vertx.core.Promise;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletionStage;

public class CollectionObserver<V> implements StreamObserver<V> {

    private final LinkedList<V> values = new LinkedList<>();
    private final Promise<Collection<V>> promise = Promise.promise();

    @Override
    public void onNext(V value) {
        values.add(value);
    }

    @Override
    public void onError(Throwable t) {
        promise.fail(t);
    }

    @Override
    public void onCompleted() {
        promise.complete(values);
    }

    public CompletionStage<Collection<V>> toCompletionStage() {
        return promise.future().toCompletionStage();
    }

}
