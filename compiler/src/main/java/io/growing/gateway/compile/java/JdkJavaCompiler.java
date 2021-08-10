package io.growing.gateway.compile.java;

import io.growing.gateway.compile.CompilationFailedException;
import io.growing.gateway.compile.Compiler;
import io.growing.gateway.utilities.CollectionUtils;
import io.growing.gateway.utilities.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class JdkJavaCompiler implements Compiler<JavaCompileSpec> {

    @Override
    public void execute(JavaCompileSpec spec) throws CompilationFailedException {
        try {
            final boolean success = createCompileTask(spec).call();
            if (!success) {
                throw new CompilationFailedException();
            }
        } catch (IOException e) {
            throw new CompilationFailedException(e);
        }
    }

    private JavaCompiler.CompilationTask createCompileTask(final JavaCompileSpec spec) throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        final Set<Path> sources = FileUtils.listAllFiles(spec.getSources());
        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(sources);
        final List<String> options = createCompileOptions(spec);
        return compiler.getTask(null, fileManager, null, options, options, compilationUnits);
    }


    private List<String> createCompileOptions(final JavaCompileSpec spec) throws IOException {
        final List<String> options = new LinkedList<>();
        if (Objects.nonNull(spec.getDestination())) {
            options.add("-d");
            options.add(spec.getDestination().toString());
        }
        final Set<Path> libs = FileUtils.listAllFiles(spec.getLibs());
        if (CollectionUtils.isNotEmpty(libs)) {
            try (final Stream<Path> stream = libs.stream()) {
                final String classpath = stream.map(path -> path.toAbsolutePath().toString()).collect(Collectors.joining(File.pathSeparator));
                options.add("-cp");
                options.add(classpath);
            }
        }
        if (StringUtils.isNoneBlank(spec.getSourceCompatibility())) {
            options.add("-source");
            options.add(spec.getSourceCompatibility());
        }
        if (StringUtils.isNoneBlank(spec.getTargetCompatibility())) {
            options.add("-target");
            options.add(spec.getTargetCompatibility());
        }
        return options;
    }

}
