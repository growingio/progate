package io.growing.progate.restful.config;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestfulConfig {
    private String path;
    private List<Map<String, String>> scalars;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getScalars() {
        if (Objects.isNull(scalars) || scalars.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> allScalars = new LinkedHashMap<>();
        scalars.forEach(allScalars::putAll);
        return allScalars;
    }

    public void setScalars(List<Map<String, String>> scalars) {
        this.scalars = scalars;
    }

}
