package io.growing.gateway.compile;

/**
 * @author AI
 */
public class CompilationFailedException extends RuntimeException {

    public CompilationFailedException() {
        super("Compilation failed; see the compiler error output for details.");
    }

    public CompilationFailedException(String message) {
        super(message);
    }

    public CompilationFailedException(Throwable cause) {
        super(cause);
    }

}
