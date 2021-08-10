package io.growing.gateway.compile.java;

import io.growing.gateway.compile.CompileSpec;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author AI
 */
public class JavaCompileSpec implements CompileSpec {
    private Set<Path> libs;
    private Path destination;
    private Set<Path> sources;
    private String sourceCompatibility;
    private String targetCompatibility;

    public Set<Path> getLibs() {
        return libs;
    }

    public void setLibs(Set<Path> libs) {
        this.libs = libs;
    }

    public Path getDestination() {
        return destination;
    }

    public void setDestination(Path destination) {
        this.destination = destination;
    }

    public Set<Path> getSources() {
        return sources;
    }

    public void setSources(Set<Path> sources) {
        this.sources = sources;
    }

    public String getSourceCompatibility() {
        return sourceCompatibility;
    }

    public void setSourceCompatibility(String sourceCompatibility) {
        this.sourceCompatibility = sourceCompatibility;
    }

    public String getTargetCompatibility() {
        return targetCompatibility;
    }

    public void setTargetCompatibility(String targetCompatibility) {
        this.targetCompatibility = targetCompatibility;
    }
}
