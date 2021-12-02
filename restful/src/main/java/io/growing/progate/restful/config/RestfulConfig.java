package io.growing.progate.restful.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class RestfulConfig {
    private String path;
    private Map<String, String> scalars;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getScalars() {
        if (Objects.isNull(scalars)) {
            return Collections.emptyMap();
        }
        return scalars;
    }

    public void setScalars(Map<String, String> scalars) {
        this.scalars = scalars;
    }

}
