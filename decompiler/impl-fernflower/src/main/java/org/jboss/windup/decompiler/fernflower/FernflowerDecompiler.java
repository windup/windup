package org.jboss.windup.decompiler.fernflower;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.util.Checks;
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
public class FernflowerDecompiler implements Decompiler
{
    private static final Logger LOG = Logger.getLogger(FernflowerDecompiler.class.getName());

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int numberOfThreads = 1;

    public FernflowerDecompiler()
    {
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

    private FernFlowerResultSaver getResultSaver(final String classFile, final File outputDirectory, final DecompilationListener listener)
    {
        return new FernFlowerResultSaver(classFile, outputDirectory, listener);
    }

    @Override
    public void decompileClassFiles(Collection<ClassDecompileRequest> allRequests, final DecompilationListener listener)
    {
        Map<String, List<ClassDecompileRequest>> requestMap = new HashMap<>();
        for (ClassDecompileRequest request : allRequests)
        {

            /*
             * Combine requests that are related (for example Foo.class and Foo$1.class), as this helps fernflower to resolve inner classes.
             */
            String filename = request.getClassFile().getFileName().toString();
            String key;
            if (filename.matches(".*\\$.*.class"))
            {
                key = request.getClassFile().getParent().resolve(filename.substring(0, filename.indexOf("$")) + ".class").toString();
            }
            else
            {
                key = request.getClassFile().toString();
            }

            List<ClassDecompileRequest> list = requestMap.get(key);
            if (list == null)
            {
                list = new ArrayList<>();
                requestMap.put(key, list);
            }
            list.add(request);
        }

        List<Callable<Void>> tasks = new ArrayList<>(requestMap.size());
        for (Map.Entry<String, List<ClassDecompileRequest>> entry : requestMap.entrySet())
        {
            final String key = entry.getKey();
            final List<ClassDecompileRequest> requests = entry.getValue();

            Callable<Void> task = new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    ClassDecompileRequest firstRequest = requests.get(0);
                    FernFlowerResultSaver resultSaver = getResultSaver(
                                firstRequest.getClassFile().toString(),
                                firstRequest.getOutputDirectory().toFile(),
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
                            listener.decompilationFailed(key, "File was not decompiled!");
                    }
                    catch (Throwable t)
                    {
                        listener.decompilationFailed(key, "Decompilation failed due to: " + t.getMessage());
                        LOG.warning("Decompilation of " + key + " failed due to: " + t.getMessage());
                    }

                    return null;
                }
            };
            tasks.add(task);
        }

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
    public DecompilationResult decompileClassFile(Path rootDir, Path classFilePath, Path outputDir) throws DecompilationException
    {
        final DecompilationResult result = new DecompilationResult();
        DecompilationListener listener = new DecompilationListener()
        {
            @Override
            public void fileDecompiled(String inputPath, String outputPath)
            {
                result.addDecompiled(inputPath, outputPath);
            }

            @Override
            public void decompilationFailed(String inputPath, String message)
            {
                result.addFailure(new DecompilationFailure(message, inputPath, null));
            }

            @Override
            public void decompilationProcessComplete()
            {

            }
        };

        FernFlowerResultSaver resultSaver = getResultSaver(classFilePath.toString(), outputDir.toFile(), listener);
        Fernflower fernflower = new Fernflower(getByteCodeProvider(), resultSaver, getOptions(), new FernflowerJDKLogger());
        fernflower.getStructContext().addSpace(classFilePath.toFile(), true);
        fernflower.decompileContext();

        if (!resultSaver.isFileSaved())
            listener.decompilationFailed(classFilePath.toString(), "File was not decompiled!");

        return result;
    }

    @Override
    public DecompilationResult decompileArchive(Path archive, Path outputDir, DecompilationListener listener) throws DecompilationException
    {
        return decompileArchive(archive, outputDir, null, listener);
    }

    @Override
    public DecompilationResult decompileArchive(Path archive, Path outputDir, Filter<ZipEntry> filter, final DecompilationListener delegate)
                throws DecompilationException
    {
        Checks.checkFileToBeRead(archive.toFile(), "Archive to decompile");
        Checks.checkDirectoryToBeFilled(outputDir.toFile(), "Output directory");

        final DecompilationResult result = new DecompilationResult();

        DecompilationListener listener = new DecompilationListener()
        {
            @Override
            public void fileDecompiled(String inputPath, String outputPath)
            {
                result.addDecompiled(inputPath, outputPath);
                delegate.fileDecompiled(inputPath, outputPath);
            }

            @Override
            public void decompilationFailed(String inputPath, String message)
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

                FernFlowerResultSaver resultSaver = getResultSaver(entry.getName(), outputDir.toFile(), listener);
                Fernflower fernflower = new Fernflower(bytecodeProvider, resultSaver, getOptions(), new FernflowerJDKLogger());
                fernflower.getStructContext().addSpace(new File(entry.getName()), true);
                fernflower.decompileContext();

                if (!resultSaver.isFileSaved())
                    listener.decompilationFailed(entry.getName(), "File was not decompiled!");
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
