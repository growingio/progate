package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;

public class AccessLogFetcher implements DataFetcher<CompletionStage<?>> {
    private final Logger logger = LoggerFactory.getLogger("access");
    private final String name;
    private final DataFetcher<CompletionStage<?>> next;

    public AccessLogFetcher(String name, DataFetcher<CompletionStage<?>> next) {
        this.name = name;
        this.next = next;
    }

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        final long started = System.nanoTime();
        return next.get(environment).whenCompleteAsync((result, t) -> {
            if (logger.isInfoEnabled()) {
                logger.info(message(started, environment));
            }
        });
    }

    private String message(final long started, final DataFetchingEnvironment environment) {
        final double cost = (System.nanoTime() - started) / 1000000.0;
        final String requestId = environment.getExecutionId().toString();
        final String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        final String address = environment.getGraphQlContext().get("address");
        return String.format("%s - %s - [%s] \"GRAPHQL %s -\" %s - %s \"-\" \"-\"", address, requestId, now, name, started, cost);
    }

}
