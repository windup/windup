package org.jboss.windup.decompiler.fernflower;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.jboss.windup.decompiler.api.ClassDecompileRequest;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.decompiler.AbstractDecompiler;
import org.jboss.windup.decompiler.util.Filter;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

/**
 * Decompiles Java classes with the Fernflower decompiler (https://github.com/JetBrains/intellij-community/tree/master/plugins/java-decompiler/engine)
 * 
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FernflowerDecompiler extends AbstractDecompiler
{
    private static final Logger LOG = Logger.getLogger(FernflowerDecompiler.class.getName());

    public FernflowerDecompiler()
    {
    }


    private Map<String, Object> getOptions()
    {
        Map<String, Object> options = new HashMap<>();
        options.put(IFernflowerPreferences.MAX_PROCESSING_METHOD, 30);
        return options;
    }

    private IBytecodeProvider getByteCodeProvider()
    {
        return new IBytecodeProvider()
        {
            @Override
            public byte[] getBytecode(String externalPath, String internalPath) throws IOException
            {
                return InterpreterUtil.getBytes(new File(externalPath));
            }
        };
    }

    private FernFlowerResultSaver getResultSaver(final List<String> requests, File directory, final DecompilationListener listener)
    {
        return new FernFlowerResultSaver(requests,directory, listener);
    }

    @Override
    public Logger getLogger()
    {
        return LOG;
    }

    public Collection<Callable<File>> getDecompileTasks(final Map<String, List<ClassDecompileRequest>> requestMap, final DecompilationListener listener)
    {
        Collection<Callable<File>> tasks = new ArrayList<>(requestMap.size());
        for (Map.Entry<String, List<ClassDecompileRequest>> entry : requestMap.entrySet())
        {
            final String key = entry.getKey();
            final List<ClassDecompileRequest> requests = entry.getValue();

            Callable<File> task = new Callable<File>()
            {
                @Override
                public File call() throws Exception
                {
                    ClassDecompileRequest firstRequest = requests.get(0);
                    List<String> classFiles = pathsFromDecompilationRequests(requests);
                    FernFlowerResultSaver resultSaver = getResultSaver(
                                pathsFromDecompilationRequests(requests),firstRequest.getOutputDirectory().toFile(),
                                listener);
                    Fernflower fernflower = new Fernflower(getByteCodeProvider(), resultSaver, getOptions(), new FernflowerJDKLogger());
                    for (ClassDecompileRequest request : requests)
                    {
                        fernflower.getStructContext().addSpace(request.getClassFile().toFile(), true);
                    }
                    try
                    {
                        fernflower.decompileContext();
                        if (!resultSaver.isFileSaved())
                            listener.decompilationFailed(classFiles, "File was not decompiled!");
                    }
                    catch (Throwable t)
                    {
                        listener.decompilationFailed(classFiles, "Decompilation failed due to: " + t.getMessage());
                        LOG.warning("Decompilation of " + key + " failed due to: " + t.getMessage());

                    }

                    return null;
                }
            };
            tasks.add(task);
        }

       return tasks;
    }

    @Override
    public DecompilationResult decompileClassFile(Path rootDir, Path classFilePath, Path outputDir) throws DecompilationException
    {
        final DecompilationResult result = new DecompilationResult();
        DecompilationListener listener = new DecompilationListener()
        {
            @Override
            public void fileDecompiled(List<String> inputPath, String outputPath)
            {
                result.addDecompiled(inputPath, outputPath);
            }

            @Override
            public void decompilationFailed(List<String> inputPath, String message)
            {
                result.addFailure(new DecompilationFailure(message, inputPath, null));
            }

            @Override
            public void decompilationProcessComplete()
            {

            }
        };

        FernFlowerResultSaver resultSaver = getResultSaver(Collections.singletonList(classFilePath.toString()), outputDir.toFile(), listener);
        Fernflower fernflower = new Fernflower(getByteCodeProvider(), resultSaver, getOptions(), new FernflowerJDKLogger());
        fernflower.getStructContext().addSpace(classFilePath.toFile(), true);
        fernflower.decompileContext();

        if (!resultSaver.isFileSaved())
            listener.decompilationFailed(Collections.singletonList(classFilePath.toString()), "File was not decompiled!");

        return result;
    }



    private List<String> pathsFromDecompilationRequests(List<ClassDecompileRequest> requests)
    {
        List<String> result = new ArrayList<>();
        for(ClassDecompileRequest request : requests) {
            result.add(request.getClassFile().toString());
        }
        return result;
    }

    @Override
    public DecompilationResult decompileArchiveImpl(Path archive, Path outputDir, Filter<ZipEntry> filter, final DecompilationListener delegate)
                throws DecompilationException
    {

        final DecompilationResult result = new DecompilationResult();
        DecompilationListener listener = new DecompilationListener()
        {
            @Override
            public void fileDecompiled(List<String> inputPaths, String outputPath)
            {
                result.addDecompiled(inputPaths, outputPath);
                delegate.fileDecompiled(inputPaths, outputPath);
            }

            @Override
            public void decompilationFailed(List<String> inputPath, String message)
            {
                result.addFailure(new DecompilationFailure(message, inputPath, null));
                delegate.decompilationFailed(inputPath, message);
            }

            @Override
            public void decompilationProcessComplete()
            {
                delegate.decompilationProcessComplete();
            }
        };

        LOG.info("Decompiling archive '" + archive.toAbsolutePath() + "' to '" + outputDir.toAbsolutePath() + "'");
        final JarFile jar;
        try
        {
            jar = new JarFile(archive.toFile());
        }
        catch (IOException ex)
        {
            throw new DecompilationException("Can't load .jar: " + archive, ex);
        }

        try
        {
            final AtomicInteger jarEntryCount = new AtomicInteger(0);
            Enumeration<JarEntry> countEnum = jar.entries();
            while (countEnum.hasMoreElements())
            {
                countEnum.nextElement();
                jarEntryCount.incrementAndGet();
            }

            final AtomicInteger current = new AtomicInteger(0);
            final Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements())
            {
                final JarEntry entry = entries.nextElement();

                final String name = entry.getName();

                if (!name.endsWith(".class"))
                {
                    jarEntryCount.decrementAndGet();
                    continue;
                }

                if (entry.getName().contains("$"))
                    continue;

                if (filter != null)
                {
                    Filter.Result filterRes = filter.decide(entry);

                    if (filterRes == Filter.Result.REJECT)
                    {
                        jarEntryCount.decrementAndGet();
                        continue;
                    }
                    else if (filterRes == Filter.Result.STOP)
                    {
                        break;
                    }
                }

                IBytecodeProvider bytecodeProvider = new IBytecodeProvider()
                {
                    @Override
                    public byte[] getBytecode(String externalPath, String internalPath) throws IOException
                    {
                        return InterpreterUtil.getBytes(jar, entry);
                    }
                };

                FernFlowerResultSaver resultSaver = getResultSaver(Collections.singletonList(entry.getName()), outputDir.toFile(), listener);
                Fernflower fernflower = new Fernflower(bytecodeProvider, resultSaver, getOptions(), new FernflowerJDKLogger());
                fernflower.getStructContext().addSpace(new File(entry.getName()), true);
                fernflower.decompileContext();

                if (!resultSaver.isFileSaved())
                    listener.decompilationFailed(Collections.singletonList(entry.getName()), "File was not decompiled!");
            }
            listener.decompilationProcessComplete();
            return result;
        }
        finally
        {
            try
            {
                jar.close();
            }
            catch (IOException e)
            {
                LOG.warning("Failed to close jar file: " + jar.getName());
            }
        }
    }

}
