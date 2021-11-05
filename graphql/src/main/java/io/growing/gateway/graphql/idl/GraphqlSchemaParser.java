package io.growing.gateway.graphql.idl;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.function.TriConsumer;
import io.growing.gateway.meta.EndpointDefinition;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.utilities.CollectionUtilities;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public class GraphqlSchemaParser {

    private final char end = '}';
    private final Pattern queryPattern = Pattern.compile("type +\\w*Query +\\{ ?");
    private final Pattern mutationPattern = Pattern.compile("type +\\w*Mutation +\\{ ?");
    private final Logger logger = LoggerFactory.getLogger(GraphqlSchemaParser.class);

    public TypeDefinitionRegistry parse(final List<ServiceMetadata> services) {
        return parse((schemas, queries, mutations) -> services.forEach(service -> appendGraphqlDefinition(service, schemas, queries, mutations)));
    }

    public TypeDefinitionRegistry parse(final ServiceMetadata service) {
        return parse((schemas, queries, mutations) -> appendGraphqlDefinition(service, schemas, queries, mutations));
    }

    private TypeDefinitionRegistry parse(final TriConsumer<StringBuilder, StringBuilder, StringBuilder> function) {
        final StringBuilder queries = new StringBuilder();
        final StringBuilder mutations = new StringBuilder();
        final StringBuilder schemas = new StringBuilder();
        function.apply(schemas, queries, mutations);
        return new SchemaParser().parse(toGraphqlSchema(schemas, queries, mutations));
    }

    private String toGraphqlSchema(final StringBuilder schemas, final StringBuilder queries, final StringBuilder mutations) {
        final StringBuilder results = new StringBuilder(schemas);
        if (queries.length() > 0) {
            results.append("type Query {\n").append(queries).append(StringUtils.LF).append(end).append(StringUtils.LF);
        }
        if (mutations.length() > 0) {
            results.append("type Mutation {\n").append(mutations).append(StringUtils.LF).append(end);
        }
        return results.toString();
    }

    private void appendGraphqlDefinition(final ServiceMetadata service, final StringBuilder schemas, final StringBuilder queries, final StringBuilder mutations) {
        if (CollectionUtilities.isNotEmpty(service.graphqlDefinitions())) {
            try {
                for (EndpointDefinition def : service.graphqlDefinitions()) {
                    final CharSource source = ByteSource.wrap(def.getContent()).asCharSource(StandardCharsets.UTF_8);
                    if (def.getName().contains(".schema.")) {
                        schemas.append(source.read());
                        continue;
                    }
                    final List<String> lines = source.readLines();
                    boolean matched = false;
                    boolean isQuery = false;
                    for (String line : lines) {
                        if (queryPattern.matcher(line).matches()) {
                            matched = true;
                            isQuery = true;
                            continue;
                        } else if (mutationPattern.matcher(line).matches()) {
                            matched = true;
                            isQuery = false;
                            continue;
                        } else if (matched && line.lastIndexOf(end) > -1) {
                            matched = false;
                            continue;
                        }
                        if (matched) {
                            if (isQuery) {
                                queries.append(line).append(StringUtils.LF);
                            } else {
                                mutations.append(line).append(StringUtils.LF);
                            }
                        } else {
                            if (schemas.indexOf(line) < 0) {
                                schemas.append(line).append(StringUtils.LF);
                            }
                        }
                    }

                }
            } catch (IOException e) {
                logger.warn("Cannot load endpoint definition: " + service.upstream().name(), e);
            }
        }
    }

}
