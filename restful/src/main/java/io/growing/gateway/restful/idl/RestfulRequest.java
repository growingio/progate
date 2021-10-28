package io.growing.gateway.restful.idl;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Map;

/**
 * @Description: 描述
 * @Author: zhuhongbin
 * @Date 2021/10/28 5:23 下午
 **/
public class RestfulRequest {
    private List<Parameter> parameters;
    private Map<String, Schema> maps;

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Schema> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, Schema> maps) {
        this.maps = maps;
    }

    public RestfulRequest() {
    }

    public RestfulRequest(List<Parameter> parameters, Map<String, Schema> maps) {
        this.parameters = parameters;
        this.maps = maps;
    }
}
