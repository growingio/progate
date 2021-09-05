package io.growing.gateway.graphql.fetcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.transcode.ResultWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<CompletionStage<?>> {
    private final String endpoint;
    private final Upstream upstream;
    private final Outgoing outgoing;
    private final List<String> values;
    private final List<String> mappings;
    private final boolean isListReturnType;

    public OutgoingDataFetcher(String endpoint, Upstream upstream, Outgoing outgoing,
                               List<String> values, List<String> mappings, boolean isListReturnType) {
        this.endpoint = endpoint;
        this.upstream = upstream;
        this.outgoing = outgoing;
        this.values = values;
        this.mappings = mappings;
        this.isListReturnType = isListReturnType;
    }

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        //
        final RequestContext context = new DataFetchingEnvironmentContext(environment, values, mappings);
        final CompletionStage<?> stage = outgoing.handle(upstream, endpoint, context);
        return stage.thenApply(result -> {
            final Object value = wrap(result);
            if (!isListReturnType && value instanceof Collection) {
                final Iterator<?> iterator = ((Collection<?>) value).iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return null;
            }
            return value;
        });
        //
    }

    @SuppressWarnings("unchecked")
    private Object wrap(final Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).stream().map(v -> {
                if (v instanceof Map) {
                    return new ResultWrapper((Map<String, Object>) v);
                }
                return v;
            }).collect(Collectors.toList());
        }
        if (value instanceof Map) {
            return new ResultWrapper((Map<String, Object>) value);
        }
        return value;
    }

    private String toGraphqlStatement(final DataFetchingEnvironment environment) throws JsonProcessingException {
        final Field field = environment.getField();
        final GraphQLObjectType type = (GraphQLObjectType) environment.getParentType();
        final StringBuilder sb = new StringBuilder();
        sb.append(type.getName().toLowerCase()).append("{").append(field.getName());
        if (Objects.nonNull(environment.getArguments()) && environment.getArguments().size() > 0) {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature());
            final String arguments = objectMapper.writeValueAsString(environment.getArguments());
            sb.append("(").append(arguments, 1, arguments.length() - 1).append(")");
        }
        final List<String> selectionSets = environment.getSelectionSet().getFields().stream().map(SelectedField::getName).collect(Collectors.toList());
        sb.append("{").append(StringUtils.join(selectionSets, ",")).append("}").append("}");
        return sb.toString();
    }

}
