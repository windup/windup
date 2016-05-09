package org.jboss.windup.decompiler.decompiler;

import org.jboss.windup.decompiler.api.ClassDecompileRequest;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.util.Checks;
import org.jboss.windup.util.threading.WindupExecutors;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 *  An abstract class encapsulating the common logic from the {@link org.jboss.windup.decompiler.api.Decompiler} implementations.
 * @author <a href="mailto:mbriskar@redhat.com">Matej Briskar</a>
 */
public abstract class AbstractDecompiler implements Decompiler
{
    private ExecutorService executorService = WindupExecutors.newSingleThreadExecutor();
    private int numberOfThreads = 1;

    public abstract Logger getLogger();

    public void setExecutorService(ExecutorService service, int numberOfThreads)
    {
        this.executorService.shutdown();
        try
        {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Was not able to decompile in the given time limit.");
        }
        this.numberOfThreads = numberOfThreads;
        this.executorService = service;
    }

    protected Map<String, List<ClassDecompileRequest>> groupDecompileRequests(final Collection<ClassDecompileRequest> requests) {
        Map<String, List<ClassDecompileRequest>> requestMap = new HashMap<>();
        for (ClassDecompileRequest request : requests)
        {

            /*
             * Combine requests that are related (for example Foo.class and Foo$1.class), as this helps fernflower to resolve inner classes.
             */
            String filename = request.getClassFile().getFileName().toString();
            String key;
            boolean mainClassFile = false;
            if (filename.matches(".*\\$.*.class"))
            {
                key = request.getClassFile().getParent().resolve(filename.substring(0, filename.indexOf("$")) + ".class").toString();
            }
            else
            {
                mainClassFile=true;
                key = request.getClassFile().toString();
            }

            List<ClassDecompileRequest> list = requestMap.get(key);
            if (list == null)
            {
                list = new ArrayList<>();
                requestMap.put(key, list);
            }
            if(mainClassFile) {
                list.add(0,request);
            } else {
                list.add(request);
            }
        }
        return requestMap;
    }

    public abstract Collection<Callable<File>> getDecompileTasks(Map<String, List<ClassDecompileRequest>> requestMap,DecompilationListener listener);

    @Override public void decompileClassFiles(Collection<ClassDecompileRequest> requests, DecompilationListener listener)
    {
        Map<String, List<ClassDecompileRequest>> requestMap = groupDecompileRequests(requests);
        Collection<Callable<File>> tasks = getDecompileTasks(requestMap,listener);
        try
        {
            executorService.invokeAll(tasks);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Decompilation was interrupted.");
        }
        finally
        {
            listener.decompilationProcessComplete();
        }
    }

    @Override
    public void close()
    {
        this.executorService.shutdown();
        try
        {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Was not able to decompile in the given time limit.");
        }
    }

    /**
     * Decompiles all .class files and nested archives in the given archive.
     * <p>
     * Nested archives will be decompiled into directories matching the name of the archive, e.g.
     * <code>foo.ear/bar.jar/src/com/foo/bar/Baz.java</code>.
     * <p>
     * Required directories will be created as needed.
     *
     * @param archive The archive containing source files and archives.
     * @param outputDir The directory where decompiled .java files will be placed.
     *
     * @returns Result with all decompilation failures. Never throws.
     */
    @Override
    public DecompilationResult decompileArchive(Path archive, Path outputDir, DecompilationListener listener) throws DecompilationException
    {
        return decompileArchive(archive, outputDir, null, listener);
    }

    @Override public DecompilationResult decompileArchive(Path archive, Path outputDir, Filter<ZipEntry> filter, DecompilationListener listener)
                throws DecompilationException
    {
        Checks.checkFileToBeRead(archive.toFile(), "Archive to decompile");
        Checks.checkDirectoryToBeFilled(outputDir.toFile(), "Output directory");
        return decompileArchiveImpl(archive, outputDir, filter, listener);
    }

    public abstract DecompilationResult decompileArchiveImpl(Path archive, Path outputDir, Filter<ZipEntry> filter, DecompilationListener listener);

    public ExecutorService getExecutorService()
    {
        return executorService;
    }

    public int getNumberOfThreads()
    {
        return numberOfThreads;
    }
}
