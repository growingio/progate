package io.growing.gateway.compile.proto;

import io.growing.gateway.compile.CompileSpec;

import java.nio.file.Path;

/**
 * @author AI
 */
public class ProtocCompileSpec implements CompileSpec {
    //    private Path imports;
    private Path source;
    private Path javaOut;
    private Path protocPlugins;
    private String protocVersion = "1.39.0";
    private String grpcPluginName = "protoc-gen-grpc-java";
    private boolean includeStandardTypes;

    public Path getSource() {
        return source;
    }

    public void setSource(Path source) {
        this.source = source;
    }

    public Path getJavaOut() {
        return javaOut;
    }

    public void setJavaOut(Path javaOut) {
        this.javaOut = javaOut;
    }

    public Path getProtocPlugins() {
        return protocPlugins;
    }

    public void setProtocPlugins(Path protocPlugins) {
        this.protocPlugins = protocPlugins;
    }

    public String getProtocVersion() {
        return protocVersion;
    }

    public void setProtocVersion(String protocVersion) {
        this.protocVersion = protocVersion;
    }

    public String getGrpcPluginName() {
        return grpcPluginName;
    }

    public void setGrpcPluginName(String grpcPluginName) {
        this.grpcPluginName = grpcPluginName;
    }

    public boolean isIncludeStandardTypes() {
        return includeStandardTypes;
    }

    public void setIncludeStandardTypes(boolean includeStandardTypes) {
        this.includeStandardTypes = includeStandardTypes;
    }
}
