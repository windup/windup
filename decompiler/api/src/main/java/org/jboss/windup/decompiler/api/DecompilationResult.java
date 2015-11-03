package org.jboss.windup.decompiler.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Keeps a count of successful decompilations and list of failed ones, in the form of an exception with String path and cause exception.
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DecompilationResult
{
    private final List<DecompilationFailure> failed = Collections.synchronizedList(new LinkedList<DecompilationFailure>());
    private final Map<String, String> decompiledFiles = Collections.synchronizedMap(new HashMap<String, String>());

    public void addDecompiled(List<String> inputPaths, String path)
    {
        String mainInput = null;
        for(String inputPath : inputPaths) {
            if(!inputPath.contains("$")) {
                mainInput=inputPath;
            }
        }
        if(mainInput==null) {
            mainInput = inputPaths.get(0);
        }
        this.decompiledFiles.put(mainInput, path);
    }

    public Map<String, String> getDecompiledFiles()
    {
        return this.decompiledFiles;
    }

    public void addFailure(DecompilationFailure failure)
    {
        this.failed.add(failure);
    }

    public List<DecompilationFailure> getFailures()
    {
        return Collections.unmodifiableList(this.failed);
    }
}
