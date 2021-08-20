package io.growing.gateway.graphql.exception;

public class FetcherNotFoundException extends RuntimeException {

    public FetcherNotFoundException() {
        super("Cannot found fetcher");
    }

}
