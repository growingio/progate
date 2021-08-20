package io.growing.gateway.graphql.idl;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.function.TriConsumer;
import io.growing.gateway.module.EndpointDefinition;
import io.growing.gateway.module.ModuleScheme;
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

    public TypeDefinitionRegistry parse(final List<ModuleScheme> schemes) {
        return parse((schemas, queries, mutations) -> schemes.forEach(scheme -> appendGraphqlDefinition(scheme, schemas, queries, mutations)));
    }

    public TypeDefinitionRegistry parse(final ModuleScheme scheme) {
        return parse((schemas, queries, mutations) -> appendGraphqlDefinition(scheme, schemas, queries, mutations));
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
        results.append("type Query {\n").append(queries).append(StringUtils.LF).append(end).append(StringUtils.LF)
            .append("type Mutation {\n").append(mutations).append(StringUtils.LF).append(end);
        return results.toString();
    }

    private void appendGraphqlDefinition(final ModuleScheme scheme, final StringBuilder schemas, final StringBuilder queries, final StringBuilder mutations) {
        if (CollectionUtilities.isNotEmpty(scheme.graphqlDefinitions())) {
            try {
                for (EndpointDefinition def : scheme.graphqlDefinitions()) {
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
                            schemas.append(line).append(StringUtils.LF);
                        }
                    }

                }
            } catch (IOException e) {
                logger.warn("Cannot load endpoint definition: " + scheme.name(), e);
            }
        }
    }

}
