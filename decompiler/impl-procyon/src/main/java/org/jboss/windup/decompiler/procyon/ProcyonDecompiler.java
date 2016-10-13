package org.jboss.windup.decompiler.procyon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.decompiler.api.ClassDecompileRequest;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.decompiler.AbstractDecompiler;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.util.Checks;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.ClasspathTypeLoader;
import com.strobel.assembler.metadata.CompositeTypeLoader;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.NoRetryMetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.BytecodeLanguage;
import com.strobel.decompiler.languages.LineNumberPosition;
import com.strobel.decompiler.languages.TypeDecompilationResults;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.io.PathHelper;

/**
 * Decompiles Java classes with Procyon Decompiler. See https://bitbucket.org/mstrobel/procyon
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompiler extends AbstractDecompiler
{
    private static final Logger log = Logger.getLogger(ProcyonDecompiler.class.getName());
    private final ProcyonConfiguration procyonConf;

    public ProcyonDecompiler()
    {
        this.procyonConf = new ProcyonConfiguration();
    }

    public ProcyonDecompiler(ProcyonConfiguration configuration)
    {
        if (configuration == null)
            throw new IllegalArgumentException("Configuration must not be null.");

        this.procyonConf = configuration;
    }

    @Override
    public Logger getLogger()
    {
        return log;
    }


    public Collection<Callable<File>> getDecompileTasks(final Map<String, List<ClassDecompileRequest>> requestMap, final DecompilationListener listener)
    {
        final AtomicInteger current = new AtomicInteger(0);
        Collection<Callable<File>> tasks = new ArrayList<>();

        final Map<Path, DecompilerSettings> settingsByOutputDirectory = new TreeMap<>();
        final Map<Path, Queue<MetadataSystem>> metadataSystemCaches = new TreeMap<>();
        final Map<Path, AtomicInteger> countByOutputDirectory = new TreeMap<>();

        for (Map.Entry<String, List<ClassDecompileRequest>> entry : requestMap.entrySet())
        {
            ClassDecompileRequest mainRequest = entry.getValue().get(0);
            if (!settingsByOutputDirectory.containsKey(mainRequest.getOutputDirectory()))
            {
                final DecompilerSettings settings = getDefaultSettings(mainRequest.getOutputDirectory().toFile());
                final ITypeLoader typeLoader = new CompositeTypeLoader(new ClasspathTypeLoader(mainRequest.getRootDirectory().toString()),
                            new ClasspathTypeLoader());
                settings.setTypeLoader(typeLoader);
                settingsByOutputDirectory.put(mainRequest.getOutputDirectory(), settings);

                final Queue<MetadataSystem> metadataSystemCache = new LinkedList<>();
                refreshMetadataCache(metadataSystemCache, settings);
                metadataSystemCaches.put(mainRequest.getOutputDirectory(), metadataSystemCache);

                countByOutputDirectory.put(mainRequest.getOutputDirectory(), new AtomicInteger(1));
            }
            else
            {
                countByOutputDirectory.get(mainRequest.getOutputDirectory()).incrementAndGet();
            }
        }

        for (final Map.Entry<String, List<ClassDecompileRequest>> entry : requestMap.entrySet())
        {
            final ClassDecompileRequest mainRequest = entry.getValue().get(0);

            // TODO - This approach is a hack, but it should work around the Procyon decompiler hangs for now
            Callable<File> callable = new Callable<File>()
            {
                @Override
                public File call() throws Exception
                {
                    List<String> classFilePaths = pathsFromDecompilationRequests(entry.getValue());
                    final DecompilerSettings settings = settingsByOutputDirectory.get(mainRequest.getOutputDirectory());
                    Queue<MetadataSystem> metadataSystemCache = metadataSystemCaches.get(mainRequest.getOutputDirectory());

                    MetadataSystem metadataSystem = null;
                    try
                    {
                        synchronized (metadataSystemCache)
                        {
                            if (current.incrementAndGet() % 50 == 0)
                            {
                                log.info("Decompiling " + current + " / " + requestMap.size());
                                refreshMetadataCache(metadataSystemCache, settings);
                            }
                            metadataSystem = metadataSystemCache.remove();
                        }

                        ExecutionStatistics.get().begin("ProcyonDecompiler.decompileIndividualItem");
                        String typeName = mainRequest.getClassFile().normalize().toAbsolutePath().toString()
                                    .substring(mainRequest.getRootDirectory().normalize().toAbsolutePath().toString().length() + 1);
                        typeName = StringUtils.removeEnd(typeName, ".class");
                        final DecompileExecutor t = new DecompileExecutor(settings, metadataSystem, typeName);
                        // TODO - This approach is a hack, but it should work around the Procyon decompiler hangs
                        // for now
                        t.start();
                        t.join(60000L); // wait up to ten seconds
                        if (!t.success)
                        {
                            if (t.e == null)
                            {
                                t.cancelDecompilation();
                                throw new RuntimeException("Failed to decompile within 60 seconds... attempting abort", t.e);
                            }
                            else
                            {
                                throw new RuntimeException(t.e);
                            }
                        }

                        File outputFile = t.outputFile;
                        if (outputFile != null)
                            listener.fileDecompiled(classFilePaths, outputFile.getAbsolutePath());
                        return outputFile;
                    }
                    catch (Throwable th)
                    {
                        String msg = "Error during decompilation of " + mainRequest.getClassFile() + ": " + th.getMessage();
                        DecompilationFailure ex = new DecompilationFailure(msg, classFilePaths, th);
                        log.log(Level.SEVERE, msg, ex);
                        listener.decompilationFailed(classFilePaths, msg);
                    }
                    finally
                    {
                        if (metadataSystem != null)
                        {
                            synchronized (metadataSystemCache)
                            {
                                metadataSystemCache.add(metadataSystem);
                            }
                        }

                        if (countByOutputDirectory.get(mainRequest.getOutputDirectory()).decrementAndGet() == 0)
                        {
                            settingsByOutputDirectory.remove(mainRequest.getOutputDirectory());
                            metadataSystemCaches.remove(mainRequest.getOutputDirectory());
                        }
                        ExecutionStatistics.get().end("ProcyonDecompiler.decompileIndividualItem");
                    }
                    return null;
                }
            };
            tasks.add(callable);
        }
        return tasks;

    }

    private List<String> pathsFromDecompilationRequests(List<ClassDecompileRequest> requests) {
        List<String> result = new ArrayList<>();
        for(ClassDecompileRequest request : requests) {
            result.add(request.getClassFile().toString());
        }
        return result;
    }

    /**
     * Decompiles the given .class file and creates the specified output source file.
     * 
     * @param classFilePath the .class file to be decompiled.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    @Override
    public DecompilationResult decompileClassFile(Path rootDir, Path classFilePath, Path outputDir)
                throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir.toFile(), "Classes root dir");
        File classFile = classFilePath.toFile();
        Checks.checkFileToBeRead(classFile, "Class file");
        Checks.checkDirectoryToBeFilled(outputDir.toFile(), "Output directory");

        log.info("Decompiling .class '" + classFilePath + "' to '" + outputDir + "' from: '" + rootDir + "'");

        String name = classFilePath.normalize().toAbsolutePath().toString().substring(rootDir.toAbsolutePath().toString().length() + 1);
        final String typeName = StringUtils.removeEnd(name, ".class");// .replace('/', '.');

        DecompilationResult result = new DecompilationResult();
        try
        {
            DecompilerSettings settings = getDefaultSettings(outputDir.toFile());
            this.procyonConf.setDecompilerSettings(settings); // TODO: This is horrible mess.

            ITypeLoader typeLoader = new CompositeTypeLoader(new ClasspathTypeLoader(rootDir.toString()), new ClasspathTypeLoader());
            MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
            File outputFile = this.decompileType(settings, metadataSystem, typeName);
            result.addDecompiled(Collections.singletonList(classFilePath.toString()), outputFile.getAbsolutePath());
        }
        catch (Throwable e)
        {
            DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                        + classFilePath.toString() + ":\n    " + e.getMessage(), Collections.singletonList(name), e);
            log.severe(failure.getMessage());
            result.addFailure(failure);
        }

        return result;
    }



    private void decompileDirectory(final Path rootDir, Path outputDir, Path subPath, final DecompilationResult result)
                throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir.toFile(), "Directory to decompile");
        Checks.checkDirectoryToBeFilled(outputDir.toFile(), "Output directory");

        log.info("Decompiling subdir '" + subPath + "'");

        /*
         * This forces an initialization of the settings.
         */
        final DecompilerSettings settings = getDefaultSettings(outputDir.toFile());

        // TODO: Rewrite with Commons IO's DirectoryWalker.
        File curDirFull = rootDir.resolve(subPath).toFile();
        final List<File> files = Arrays.asList(curDirFull.listFiles());
        Collection<Callable<File>> tasks = new ArrayList<>();
        for (File file : files)
        {
            final MetadataSystem metadataSystem = new NoRetryMetadataSystem(new InputTypeLoader());
            if (file.isDirectory())
            {
                Path subPathNew = subPath.resolve(file.getName());
                decompileDirectory(rootDir, outputDir, subPathNew, result);
                continue;
            }

            if (!file.getName().endsWith(".class"))
                continue;

            if (file.getName().contains("$"))
                continue;

            final String fileSubPath = subPath.resolve(file.getName()).toString();
            final String fqcn = StringUtils.removeEnd(fileSubPath, ".class").replace('/', '.');
            final String fileAbsolutePath = file.getAbsolutePath();

            Callable<File> callable = new Callable<File>()
            {
                @Override
                public File call() throws Exception
                {
                    File outputFile;
                    try
                    {
                        outputFile = decompileType(settings, metadataSystem, fqcn);
                        if (null == outputFile)
                            throw new IllegalStateException("Unknown Procyon error, type not found.");
                        result.addDecompiled(Collections.singletonList(fileAbsolutePath), outputFile.getAbsolutePath());
                        return outputFile;
                    }
                    catch (Exception e)
                    {
                        DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                                    + rootDir + " / " + fileSubPath + ":\n    " + e.getMessage(),
                                    Collections.singletonList(fileSubPath.toString()), e);
                        log.log(Level.SEVERE, failure.getMessage(), failure);
                        result.addFailure(failure);
                    }
                    return null;
                }

            };
            tasks.add(callable);
        }
        try
        {
            getExecutorService().invokeAll(tasks);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Was not able to decompile in the given time limit.");
        }
    }

    /**
     * Decompiles .class files and nested archives in the given archive, as allowed by the given filter.
     * <p>
     * Nested archives will be decompiled into directories matching the name of the archive, e.g.
     * <code>foo.ear/bar.jar/src/com/foo/bar/Baz.java</code>.
     * <p>
     * Required directories will be created as needed.
     * 
     * @param archive The archive containing source files and archives.
     * @param outputDir The directory where decompiled .java files will be placed.
     * @param filter Decides which classes will be decompiled.
     * 
     * @returns Result with all decompilation failures. Never throws.
     */
    @Override
    public DecompilationResult decompileArchiveImpl(final Path archive, Path outputDir, Filter<ZipEntry> filter, final DecompilationListener listener)
                throws DecompilationException
    {
        Checks.checkFileToBeRead(archive.toFile(), "Archive to decompile");
        Checks.checkDirectoryToBeFilled(outputDir.toFile(), "Output directory");

        log.info("Decompiling archive '" + archive.toAbsolutePath() + "' to '" + outputDir.toAbsolutePath() + "'");

        JarFile jar = loadJar(archive.toFile());
        try
        {
            final AtomicInteger jarEntryCount = new AtomicInteger(0);
            Enumeration<JarEntry> countEnum = jar.entries();
            while (countEnum.hasMoreElements())
            {
                countEnum.nextElement();
                jarEntryCount.incrementAndGet();
            }

            // MetadataSystem, TypeLoader's
            final DecompilerSettings settings = getDefaultSettings(outputDir.toFile());
            settings.setTypeLoader(new CompositeTypeLoader(new WindupJarTypeLoader(jar), settings.getTypeLoader()));

            final DecompilationResult res = new DecompilationResult();

            final AtomicInteger current = new AtomicInteger(0);
            final Enumeration<JarEntry> entries = jar.entries();
            Collection<Callable<File>> tasks = new ArrayList<>();

            final Queue<MetadataSystem> metadataSystemCache = new LinkedList<>();
            refreshMetadataCache(metadataSystemCache, settings);

            while (entries.hasMoreElements())
            {
                final JarEntry entry = entries.nextElement();

                final String name = entry.getName();

                if (!name.endsWith(".class"))
                {
                    jarEntryCount.decrementAndGet();
                    continue;
                }

                if (filter != null)
                {
                    Filter.Result filterRes = filter.decide(entry);

                    if (filterRes == Filter.Result.REJECT)
                    {
                        jarEntryCount.decrementAndGet();
                        continue;
                    }
                    if (filterRes == Filter.Result.STOP)
                    {
                        break;
                    }
                }

                final String typeName = StringUtils.removeEnd(name, ".class");

                // TODO - This approach is a hack, but it should work around the Procyon decompiler hangs for now
                Callable<File> callable = new Callable<File>()
                {
                    @Override
                    public File call() throws Exception
                    {
                        MetadataSystem metadataSystem = null;
                        try
                        {
                            synchronized (metadataSystemCache)
                            {
                                if (current.incrementAndGet() % 50 == 0)
                                {
                                    log.info("Decompiling " + current + " / " + jarEntryCount);
                                    refreshMetadataCache(metadataSystemCache, settings);
                                }
                                metadataSystem = metadataSystemCache.remove();
                            }

                            ExecutionStatistics.get().begin("ProcyonDecompiler.decompileIndividualItem");
                            final DecompileExecutor t = new DecompileExecutor(settings, metadataSystem, typeName);
                            // TODO - This approach is a hack, but it should work around the Procyon decompiler hangs
                            // for now
                            t.start();
                            t.join(60000L); // timeout if it is taking too long
                            if (!t.success)
                            {
                                if (t.e == null)
                                {
                                    t.cancelDecompilation();
                                    throw new RuntimeException("Failed to decompile file within 60 seconds... attempting abort", t.e);
                                }
                                else
                                {
                                    throw new RuntimeException(t.e);
                                }
                            }

                            File outputFile = t.outputFile;
                            if (outputFile != null)
                            {
                                listener.fileDecompiled(Collections.singletonList(name), outputFile.getAbsolutePath());
                                res.addDecompiled(Collections.singletonList(name), outputFile.getAbsolutePath());
                            }
                            return outputFile;
                        }
                        catch (Throwable th)
                        {
                            String msg = "Error during decompilation of " + archive.toString() + "!" + name + ":\n    "
                                        + th.getMessage();
                            DecompilationFailure ex = new DecompilationFailure(msg, Collections.singletonList(name), th);
                            log.log(Level.SEVERE, msg, ex);
                            res.addFailure(ex);
                        }
                        finally
                        {
                            if (metadataSystem != null)
                            {
                                synchronized (metadataSystemCache)
                                {
                                    metadataSystemCache.add(metadataSystem);
                                }
                            }
                            ExecutionStatistics.get().end("ProcyonDecompiler.decompileIndividualItem");
                        }
                        return null;
                    }
                };
                tasks.add(callable);
            }
            try
            {
                getExecutorService().invokeAll(tasks);
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException("Decompilation was interrupted.");
            }
            finally
            {
                listener.decompilationProcessComplete();
            }
            return res;
        }
        finally
        {
            try
            {
                jar.close();
            }
            catch (IOException e)
            {
                log.warning("Failed to close jar file: " + jar.getName());
            }
        }
    }

    /**
     * The metadata cache can become huge over time. This simply flushes it periodically.
     */
    private void refreshMetadataCache(final Queue<MetadataSystem> metadataSystemCache, final DecompilerSettings settings)
    {
        metadataSystemCache.clear();
        for (int i = 0; i < this.getNumberOfThreads(); i++)
        {
            metadataSystemCache.add(new NoRetryMetadataSystem(settings.getTypeLoader()));
        }
    }

    private class DecompileExecutor extends Thread
    {
        private DecompilerSettings settings;
        private MetadataSystem metadataSystem;
        private String typeName;
        private Exception e;
        private File outputFile;
        private boolean success;

        public DecompileExecutor(DecompilerSettings settings, MetadataSystem metadataSystem, String typeName)
        {
            this.settings = settings;
            this.metadataSystem = metadataSystem;
            this.typeName = typeName;
            setDaemon(true);
        }

        @Override
        public void run()
        {
            try
            {
                this.outputFile = decompileType(settings, metadataSystem, typeName);
                this.success = true;
            }
            catch (Exception e)
            {
                this.e = e;
            }
        }

        @SuppressWarnings("deprecation")
        public void cancelDecompilation()
        {
            this.interrupt();

            // sleep up to 10 seconds
            for (int i = 0; i < 10; i++)
            {
                if (this.isAlive())
                {
                    try
                    {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e)
                    {
                        throw new WindupException("Interrupted while attempting to abort thread", e);
                    }
                }
            }
            if (this.isAlive())
            {
                // make one last (desperate) attempt to kill it
                this.stop();
            }
        }
    }

    /**
     * Decompiles a single type.
     * 
     * @param metadataSystem
     * @param typeName
     * @return
     * @throws IOException
     */
    private File decompileType(final DecompilerSettings settings, final MetadataSystem metadataSystem, final String typeName) throws IOException
    {
        log.fine("Decompiling " + typeName);

        final TypeReference type;

        // Hack to get around classes whose descriptors clash with primitive types.
        if (typeName.length() == 1)
        {
            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(typeName);
            type = metadataSystem.resolve(reference);
        }
        else
            type = metadataSystem.lookupType(typeName);

        if (type == null)
        {
            log.severe("Failed to load class: " + typeName);
            return null;
        }

        final TypeDefinition resolvedType = type.resolve();
        if (resolvedType == null)
        {
            log.severe("Failed to resolve type: " + typeName);
            return null;
        }

        boolean nested = resolvedType.isNested() || resolvedType.isAnonymous() || resolvedType.isSynthetic();
        if (!this.procyonConf.isIncludeNested() && nested)
            return null;

        settings.setFormattingOptions(new JavaFormattingOptions());

        final FileOutputWriter writer = createFileWriter(resolvedType, settings);
        final PlainTextOutput output;

        output = new PlainTextOutput(writer);
        output.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());
        if (settings.getLanguage() instanceof BytecodeLanguage)
            output.setIndentToken("  ");

        DecompilationOptions options = new DecompilationOptions();
        options.setSettings(settings); // I'm missing why these two classes are split.

        // --------- DECOMPILE ---------
        final TypeDecompilationResults results = settings.getLanguage().decompileType(resolvedType, output, options);

        writer.flush();
        writer.close();

        // If we're writing to a file and we were asked to include line numbers in any way,
        // then reformat the file to include that line number information.
        final List<LineNumberPosition> lineNumberPositions = results.getLineNumberPositions();

        if (!this.procyonConf.getLineNumberOptions().isEmpty())
        {

            final LineNumberFormatter lineFormatter = new LineNumberFormatter(writer.getFile(), lineNumberPositions,
                        this.procyonConf.getLineNumberOptions());

            lineFormatter.reformatFile();
        }
        return writer.getFile();
    }

    /**
     * Default settings set type loader to ClasspathTypeLoader if not set before.
     */
    private DecompilerSettings getDefaultSettings(File outputDir)
    {
        DecompilerSettings settings = new DecompilerSettings();
        procyonConf.setDecompilerSettings(settings);
        settings.setOutputDirectory(outputDir.getPath());
        settings.setShowSyntheticMembers(false);
        settings.setForceExplicitImports(true);

        if (settings.getTypeLoader() == null)
            settings.setTypeLoader(new ClasspathTypeLoader());
        return settings;
    }

    /**
     * Opens the jar, wraps any IOException.
     */
    private JarFile loadJar(File archive) throws DecompilationException
    {
        try
        {
            return new JarFile(archive);
        }
        catch (IOException ex)
        {
            throw new DecompilationException("Can't load .jar: " + archive.getPath(), ex);
        }
    }

    /**
     * Constructs the path from FQCN, validates writability, and creates a writer.
     */
    private static synchronized FileOutputWriter createFileWriter(final TypeDefinition type, final DecompilerSettings settings)
                throws IOException
    {
        final String outputDirectory = settings.getOutputDirectory();

        final String fileName = type.getName() + settings.getLanguage().getFileExtension();
        final String packageName = type.getPackageName();

        // foo.Bar -> foo/Bar.java
        final String subDir = StringUtils.defaultIfEmpty(packageName, "").replace('.', File.separatorChar);
        final String outputPath = PathHelper.combine(outputDirectory, subDir, fileName);

        final File outputFile = new File(outputPath);
        final File parentDir = outputFile.getParentFile();

        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs())
        {
            throw new IllegalStateException("Could not create directory:" + parentDir);
        }

        if (!outputFile.exists() && !outputFile.createNewFile())
        {
            throw new IllegalStateException("Could not create output file: " + outputPath);
        }

        return new FileOutputWriter(outputFile, settings);
    }

}
