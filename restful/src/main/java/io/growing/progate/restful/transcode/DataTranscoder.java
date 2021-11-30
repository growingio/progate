package io.growing.progate.restful.transcode;

public interface DataTranscoder<I, O> {

    O serialize(Object result);

    I parseValue(Object input);

}
