package io.growing.progate.grpc.observer;

import io.growing.gateway.grpc.observer.CollectionObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

class CollectionObserverTest {

    @Test
    void testOnCompleted() throws ExecutionException, InterruptedException {
        final CollectionObserver<Object> observer = new CollectionObserver<>();
        for (int i = 0; i < 10; i++) {
            observer.onNext(new Object());
        }
        observer.onCompleted();
        Collection<Object> objects = observer.toCompletionStage().toCompletableFuture().get();
        Assertions.assertEquals(10, objects.size());
    }

    @Test
    void testOnError() {
        final CollectionObserver<Object> observer = new CollectionObserver<>();
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
