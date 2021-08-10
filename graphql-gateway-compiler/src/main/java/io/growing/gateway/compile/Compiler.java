package io.growing.gateway.compile;

/**
 * @author AI
 */
public interface Compiler<T extends CompileSpec> {

    void execute(T spec) throws CompilationFailedException;

}
