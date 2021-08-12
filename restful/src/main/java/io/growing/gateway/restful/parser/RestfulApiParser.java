package io.growing.gateway.restful.parser;

import io.growing.gateway.restful.rule.ApiRule;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class RestfulApiParser {

    Set<ApiRule> parse(InputStream config) {
        return new HashSet<>();
    }
}
