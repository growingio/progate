package io.growing.gateway.restful.parser;

import io.growing.gateway.context.GatewayContext;
import io.growing.gateway.restful.rule.ApiRule;
import io.growing.gateway.restful.rule.PathParameterRule;
import io.growing.gateway.restful.rule.RequestRule;
import io.growing.gateway.restful.rule.ResponseRule;
import io.growing.gateway.restful.rule.config.ApiRuleConfig;
import io.growing.gateway.restful.rule.config.ApiRuleConfigs;
import io.vertx.core.http.HttpMethod;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class RestfulApiParser {

    Set<ApiRule> parse(GatewayContext context, InputStream inputStream) {
        Yaml yaml = new Yaml();

        ApiRuleConfigs apiRuleConfigs = yaml.loadAs(inputStream, ApiRuleConfigs.class);

        Set<ApiRule> apiRules = new HashSet<>();

        for (ApiRuleConfig apiRuleConfig: apiRuleConfigs.getRules()) {
            HttpMethod httpMethod = apiRuleConfig.getHttpMethod();
            String path = apiRuleConfig.getPath();
            RequestRule requestRule = apiRuleConfig.getRequest().toRule(context);
            ResponseRule responseRule = apiRuleConfig.getResponse().toRule(context);
            PathParameterRule pathParameterRule = apiRuleConfig.getParameter().toRule(context);

            ApiRule apiRule = new ApiRule(httpMethod, path,pathParameterRule, requestRule, responseRule);
            apiRules.add(apiRule);
        }

        return apiRules;

    }
}
