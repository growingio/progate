package io.growing.gateway.graphql.fetcher;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<Object> {
    private final String protocol;
    private final String endpoint;

    public OutgoingDataFetcher(String protocol, String endpoint) {
        this.protocol = protocol;
        this.endpoint = endpoint;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) throws Exception {
        final List<Map<String, String>> data = new LinkedList<>();
        data.add(ImmutableMap.of("name", "Hello"));
        return data;
    }

}
