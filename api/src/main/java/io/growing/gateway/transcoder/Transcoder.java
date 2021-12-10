package io.growing.gateway.transcoder;

public interface Transcoder {

    String name();

    Object transcode(Object from);
}
