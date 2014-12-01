package org.jboss.windup.decompiler.api;

/**
 * Called to indicate the progress during decompilation
 */
public interface DecompilationListener
{
    /**
     * Indicates that the file at inputPath has been decompiled to outputPath
     */
    public void fileDecompiled(String inputPath, String outputPath);

    /**
     * Indicates that the decompilation process is complete for all files within the archive (or directory).
     * 
     * This allows for cleanup, such as committing all results to disk.
     */
    public void decompilationProcessComplete();
}
