package org.jboss.windup.engine.decompiler;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Keeps a count of successful decompilations and list of failed ones, in the form of an exception with String path and
 * cause exception.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationResult
{
    private final List<DecompilationFailure> failed = new LinkedList<>();
    private final Set<String> decompiled = new HashSet<>();
    private final Set<String> decompiledOutputFiles = new HashSet<>();

    public void addDecompiledOutputFile(String path)
    {
        this.decompiledOutputFiles.add(path);
    }

    public Set<String> getDecompiledOutputFiles()
    {
        return this.decompiledOutputFiles;
    }

    public void addDecompiled(String path)
    {
        this.decompiled.add(path);
    }

    public void addFailure(DecompilationFailure failure)
    {
        this.failed.add(failure);
    }

    public List<DecompilationFailure> getFailures()
    {
        return Collections.unmodifiableList(this.failed);
    }

    public Set<String> getDecompiled()
    {
        return this.decompiled;
    }

}
