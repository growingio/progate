package io.growing.gateway.graphql.exception;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.execution.preparsed.persisted.PersistedQueryNotFound;
import graphql.language.SourceLocation;

import java.util.List;

public class FetcherNotFoundException extends RuntimeException implements GraphQLError {
    private final String query;

    public FetcherNotFoundException(String message, String query) {
        super(message);
        this.query = query;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return new PersistedQueryNotFound(query);
    }

}
