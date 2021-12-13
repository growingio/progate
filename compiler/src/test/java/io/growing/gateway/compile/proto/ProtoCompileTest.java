package io.growing.gateway.compile.proto;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author AI
 */
@Disabled
class ProtoCompileTest {

    @Test
    void testGetName() {
        final ProtocProtoCompiler compiler = new ProtocProtoCompiler();
        final File userDir = SystemUtils.getUserDir();
        final Path pluginPath = Paths.get(userDir.getParentFile().getAbsolutePath(), "graphql-gateway-grpc-libs", "build", "plugins");
        final ProtocCompileSpec spec = new ProtocCompileSpec();
        spec.setIncludeStandardTypes(true);
        spec.setProtocPlugins(pluginPath);
        spec.setSource(Paths.get(userDir.getAbsolutePath(), "src", "test", "proto"));
        spec.setJavaOut(Paths.get(userDir.getAbsolutePath(), "build", "grpc"));
        compiler.execute(spec);
    }
}
