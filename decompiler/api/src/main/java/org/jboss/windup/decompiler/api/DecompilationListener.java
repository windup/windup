package org.jboss.windup.decompiler.api;

import java.util.List;

/**
 * Called to indicate the progress during decompilation
 */
public interface DecompilationListener
{
    /**
     * Indicates that the files at inputPath has been decompiled to outputPath
     */
    void fileDecompiled(List<String> sourceClassPaths, String outputPath);

    /**
     * Indicates that the files at inputPath has been decompiled to outputPath
     */
    default void fileDecompiled(List<String> sourceClassPaths, String outputPath, int[] lineMapping) {
        fileDecompiled(sourceClassPaths, outputPath);
    }

    /**
     * Called to indicate that decompilation of this particular files has failed for the specified reason.
     */
    void decompilationFailed(List<String> sourceClassPaths, String message);

    /**
     * Indicates that the decompilation process is complete for all files within the archive (or directory).
     * 
     * This allows for cleanup, such as committing all results to disk.
     */
    void decompilationProcessComplete();

    /**
     * Indicates that the process has been cancelled and we should immediately halt processing.
     */
    boolean isCancelled();
}
