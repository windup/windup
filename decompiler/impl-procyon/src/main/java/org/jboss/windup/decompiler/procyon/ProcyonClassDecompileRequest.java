package org.jboss.windup.decompiler.procyon;

import java.nio.file.Path;

/**
 * Contains the information needed to decompile a single class file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProcyonClassDecompileRequest
{
    private Path rootDirectory;
    private Path classFile;
    private Path outputDirectory;

    /**
     * Create an instance with the given rootDirectory (eg, for a class named "com.foo.Example" in
     * "/path/to/com/foo/Example.class", this would be "/path/to") and the full path to the class file itself.
     *
     * The resulting decompiled file will be placed in outputDirectory.
     */
    public ProcyonClassDecompileRequest(Path rootDirectory, Path classFile, Path outputDirectory)
    {
        this.rootDirectory = rootDirectory;
        this.classFile = classFile;
        this.outputDirectory = outputDirectory;
    }

    /**
     * Contains the root directory for this class file.
     */
    public Path getRootDirectory()
    {
        return rootDirectory;
    }

    /**
     * Contains the full path to the file itself.
     */
    public Path getClassFile()
    {
        return classFile;
    }

    /**
     * Contains the full path to the output directory (where the decompiled file will be placed).
     */
    public Path getOutputDirectory()
    {
        return outputDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcyonClassDecompileRequest that = (ProcyonClassDecompileRequest) o;

        if (classFile != null ? !classFile.equals(that.classFile) : that.classFile != null) return false;
        if (outputDirectory != null ? !outputDirectory.equals(that.outputDirectory) : that.outputDirectory != null)
            return false;
        if (rootDirectory != null ? !rootDirectory.equals(that.rootDirectory) : that.rootDirectory != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rootDirectory != null ? rootDirectory.hashCode() : 0;
        result = 31 * result + (classFile != null ? classFile.hashCode() : 0);
        result = 31 * result + (outputDirectory != null ? outputDirectory.hashCode() : 0);
        return result;
    }
}
