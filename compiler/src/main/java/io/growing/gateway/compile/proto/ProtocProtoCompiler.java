package io.growing.gateway.compile.proto;

import com.github.os72.protocjar.PlatformDetector;
import com.github.os72.protocjar.Protoc;
import com.google.common.collect.Sets;
import io.growing.gateway.compile.CompilationFailedException;
import io.growing.gateway.compile.Compiler;
import io.growing.gateway.utilities.CollectionUtils;
import io.growing.gateway.utilities.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class ProtocProtoCompiler implements Compiler<ProtocCompileSpec> {
    private final Logger logger = LoggerFactory.getLogger(ProtocProtoCompiler.class);

    @Override
    public void execute(ProtocCompileSpec spec) {
        try {
            if (Files.notExists(spec.getJavaOut())) {
                Files.createDirectories(spec.getJavaOut());
            }
            final Set<Path> sources = FileUtils.listAllFiles(spec.getSource());
            Protoc.runProtoc(createProtobufCompileArgs(spec, sources));
            final Set<Path> grpcSources = selectGrpcSources(sources);
            if (CollectionUtils.isNotEmpty(grpcSources)) {
                Protoc.runProtoc(createGrpcCompileArgs(spec, grpcSources));
            }
        } catch (IOException | InterruptedException e) {
            throw new CompilationFailedException(e);
        }
    }

    private String[] createProtobufCompileArgs(final ProtocCompileSpec spec, final Set<Path> sources) {
        final List<String> args = new LinkedList<>();
        if (spec.isIncludeStandardTypes()) {
            args.add("--include_std_types");
        }
        args.add(String.format("-I=%s", spec.getSource().toAbsolutePath()));
        args.add(String.format("--java_out=%s", spec.getJavaOut().toAbsolutePath()));
        try (final Stream<Path> stream = sources.stream()) {
            stream.forEach(path -> args.add(path.toAbsolutePath().toString()));
        }
        return args.toArray(new String[0]);
    }

    private String[] createGrpcCompileArgs(final ProtocCompileSpec spec, final Set<Path> sources) throws IOException {
        final List<String> args = new LinkedList<>();
        if (spec.isIncludeStandardTypes()) {
            args.add("--include_std_types");
        }
        final String grpcPluginFilename = getGrpcPluginPath(spec);
        final Path grpcPluginPath = Paths.get(grpcPluginFilename);
        if (Files.notExists(grpcPluginPath)) {
            throw new IOException("Cannot found grpc plugin: " + grpcPluginFilename);
        }
        Files.setPosixFilePermissions(grpcPluginPath, Sets.newHashSet(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ));
        args.add(String.format("--plugin=%s=%s", spec.getGrpcPluginName(), grpcPluginFilename));
        args.add(String.format("--grpc-java_out=%s", spec.getJavaOut().toAbsolutePath()));
        args.add(String.format("--proto_path=%s", spec.getSource().toAbsolutePath()));
        sources.forEach(source -> {
            args.add(source.toAbsolutePath().toString());
        });
        return args.toArray(new String[0]);
    }

    private Set<Path> selectGrpcSources(final Set<Path> sources) {
        try (final Stream<Path> stream = sources.stream()) {
            return stream.filter(path -> {
                try (final Stream<String> lines = Files.readAllLines(path).stream()) {
                    return lines.anyMatch(line -> StringUtils.isNoneBlank(line) && line.trim().startsWith("service"));
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                    return false;
                }
            }).collect(Collectors.toSet());
        }
    }

    private String getGrpcPluginPath(final ProtocCompileSpec spec) {
        final Properties detectorProps = new Properties();
        new PlatformDetector().detect(detectorProps, null);
        final String platformClassifier = detectorProps.getProperty("os.detected.classifier");
        final String pluginFilename = String.format("%s-%s-%s.exe", spec.getGrpcPluginName(), spec.getProtocVersion(), platformClassifier);
        return spec.getProtocPlugins().resolve(pluginFilename).toAbsolutePath().toString();
    }

}
