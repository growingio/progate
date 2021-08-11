package io.growing.gateway.graphql;

import io.growing.gateway.api.Upstream;

import java.util.List;

/**
 * @author AI
 */
public interface GraphqlSchemaScanner {

    String scan(List<Upstream> upstreams);

}
