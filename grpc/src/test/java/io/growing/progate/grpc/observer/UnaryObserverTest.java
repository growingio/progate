package io.growing.progate.grpc.observer;

import io.growing.gateway.grpc.observer.UnaryObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class UnaryObserverTest {
    @Test
    void testOnCompleted() throws ExecutionException, InterruptedException {
        final UnaryObserver<Object> observer = new UnaryObserver<>();
        final Object result = new Object();
        observer.onNext(new Object());
        observer.onNext(result);
        observer.onCompleted();
        Assertions.assertEquals(result, observer.toCompletionStage().toCompletableFuture().get());
    }

    @Test
    void testOnError() {
        final UnaryObserver<Object> observer = new UnaryObserver<>();
        final Object result = new Object();
        observer.onNext(new Object());
        observer.onNext(result);
        observer.onError(new NullPointerException());
        Assertions.assertThrows(NullPointerException.class, () -> {
            try {
                observer.toCompletionStage().toCompletableFuture().get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        });
    }
}
