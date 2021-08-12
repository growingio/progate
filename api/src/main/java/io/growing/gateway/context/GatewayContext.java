package io.growing.gateway.context;

import io.growing.gateway.transcoder.Transcoder;

import java.util.ArrayList;
import java.util.Optional;

public class GatewayContext {

    ArrayList<Transcoder> registeredTranscoder;

    public void registerTranscoder(Transcoder transcoder) {
        Optional exists = getTranscoder(transcoder.name());
        if(exists.isEmpty()) {
            registeredTranscoder.add(transcoder);
        }
    }

    public Optional<Transcoder> getTranscoder(String name) {
        return registeredTranscoder.stream().filter(t -> t.name().equals(name)).findFirst();
    }
}
