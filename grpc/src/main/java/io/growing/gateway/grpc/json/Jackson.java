package io.growing.gateway.grpc.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson {
    private Jackson() {
    }

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.registerModule(DefaultScalaModule)
//        mapper.registerModule(new JavaTimeModule())
//        mapper.registerModule(new ProtobufModule())
    }
}
