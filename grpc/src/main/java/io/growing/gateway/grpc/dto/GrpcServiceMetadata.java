package io.growing.gateway.grpc.dto;

import com.google.common.collect.ImmutableList;
import io.growing.gateway.FileDescriptorDto;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.meta.EndpointDefinition;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;

import java.util.Collections;
import java.util.List;

public class GrpcServiceMetadata implements ServiceMetadata {

    private final Upstream upstream;
    private final List<EndpointDefinition> graphqlSet;
    private final List<EndpointDefinition> restfulSet;

    public static GrpcServiceMetadata form(final SchemeDto scheme, final Upstream upstream) {
        return new GrpcServiceMetadata(scheme, upstream);
    }

    private GrpcServiceMetadata(final SchemeDto scheme, final Upstream upstream) {
        this.upstream = upstream;
        if (scheme.getGraphqlDefinitionsCount() > 0) {
            final ImmutableList.Builder<EndpointDefinition> builder = new ImmutableList.Builder<>();
            scheme.getGraphqlDefinitionsList().forEach(file -> builder.add(convert(file)));
            graphqlSet = builder.build();
        } else {
            graphqlSet = Collections.emptyList();
        }
        if (scheme.getRestfulDefinitionsCount() > 0) {
            final ImmutableList.Builder<EndpointDefinition> builder = new ImmutableList.Builder<>();
            scheme.getRestfulDefinitionsList().forEach(file -> builder.add(convert(file)));
            restfulSet = builder.build();
        } else {
            restfulSet = Collections.emptyList();
        }
    }

    @Override
    public Upstream upstream() {
        return upstream;
    }

    @Override
    public List<EndpointDefinition> graphqlDefinitions() {
        return graphqlSet;
    }

    @Override
    public List<EndpointDefinition> restfulDefinitions() {
        return restfulSet;
    }

    private EndpointDefinition convert(final FileDescriptorDto descriptor) {
        return new EndpointDefinition(descriptor.getName(), descriptor.getContent().toByteArray());
    }

}
