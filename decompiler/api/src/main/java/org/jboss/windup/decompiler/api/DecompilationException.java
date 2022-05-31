package org.jboss.windup.decompiler.api;

/**
 * {@link DecompilationException} is thrown due to errors during decompiling ".jar" or ".class" files.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationException extends RuntimeException {
    private static final long serialVersionUID = -8377473815060311293L;

    public DecompilationException() {

    }

    public DecompilationException(String message) {
        super(message);
    }

    public DecompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
