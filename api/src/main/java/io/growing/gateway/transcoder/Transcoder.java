package io.growing.gateway.transcoder;

public interface Transcoder {

    Transcoder empty = new Transcoder() {
        @Override
        public String name() {
            return "empty";
        }

        @Override
        public Object transcode(Object from) {
            return from;
        }
    };

    String name();

    Object transcode(Object from);
}
