package io.growing.progate.restful.transcode;

public interface Coercing<I, O> {

    O serialize(Object result);

    I parseValue(Object input);

}
