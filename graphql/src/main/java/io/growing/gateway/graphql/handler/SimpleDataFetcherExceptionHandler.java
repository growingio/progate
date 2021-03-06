package io.growing.gateway.graphql.handler;

import com.google.common.collect.Sets;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionException;

public class SimpleDataFetcherExceptionHandler implements DataFetcherExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(SimpleDataFetcherExceptionHandler.class);

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters parameters) {
        final Throwable t = getRootCause(parameters);
        String message;
        ErrorType errorType = ErrorType.DataFetchingException;
        if (t instanceof StatusRuntimeException) {
            final StatusRuntimeException e = (StatusRuntimeException) t;
            message = e.getStatus().getDescription();
            final Status.Code code = e.getStatus().getCode();
            final Set<Status.Code> validationErrors = Sets.newHashSet(Status.Code.INVALID_ARGUMENT, Status.Code.ALREADY_EXISTS, Status.Code.PERMISSION_DENIED);
            if (validationErrors.contains(code)) {
                errorType = ErrorType.ValidationError;
            }
        } else {
            message = t.getLocalizedMessage();
        }
        if (Objects.isNull(message)) {
            message = t.getClass().getSimpleName();
        }
        logger.warn(message, t);
        final GraphQLError error = GraphqlErrorBuilder.newError(parameters.getDataFetchingEnvironment())
            .errorType(errorType).message(message).build();
        return DataFetcherExceptionHandlerResult.newResult(error).build();
    }

    private Throwable getRootCause(DataFetcherExceptionHandlerParameters parameters) {
        final Throwable t = parameters.getException();
        if (t instanceof CompletionException) {
            final CompletionException e = (CompletionException) t;
            return e.getCause();
        }
        return t;
    }

}
