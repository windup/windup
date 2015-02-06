package org.jboss.windup.decompiler.procyon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
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
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompiler implements Decompiler
{
    private ExecutorService exService = Executors.newSingleThreadExecutor();
    private int numberOfThreads = 1;
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

    public void close()
    {
        this.exService.shutdown();
        try
        {
            exService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Was not able to decompile in the given time limit.");
        }
    }

    /**
     * Decompiles the given .class file and creates the specified output source file.
     * 
     * @param classFilePath the .class file to be decompiled.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    @Override
    public DecompilationResult decompileClassFile(File rootDir, Path classFilePath, File outputDir)
                throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir, "Classes root dir");
        File classFile = rootDir.toPath().resolve(classFilePath).toFile();
        Checks.checkFileToBeRead(classFile, "Class file");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling .class '" + classFilePath + "' to '" + outputDir.getPath() + "'");

        String name = classFilePath.toString();
        final String typeName = StringUtils.removeEnd(name, ".class");// .replace('/', '.');

        DecompilationResult res = new DecompilationResult();
        try
        {
            DecompilerSettings settings = getDefaultSettings(outputDir);
            this.procyonConf.setDecompilerSettings(settings); // TODO: This is horrible mess.

            ITypeLoader typeLoader = new CompositeTypeLoader(new ClasspathTypeLoader(rootDir.getPath()),
                        new ClasspathTypeLoader());
            MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
            File outputFile = this.decompileType(metadataSystem, typeName);
            res.addDecompiled(classFilePath.toString(), outputFile.getAbsolutePath());
        }
        catch (Throwable e)
        {
            DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                        + classFilePath.toString() + ":\n    " + e.getMessage(), name, e);
            log.severe(failure.getMessage());
            res.addFailure(failure);
        }

        return res;
    }

    /**
     * Decompiles all .class files and archives in the given directory and places results in the specified output
     * directory.
     * <p>
     * Discovered archives will be decompiled into directories matching the name of the archive, e.g.
     * <code>foo.ear/bar.jar/src/com/foo/bar/Baz.java</code>.
     * <p>
     * Required directories will be created as needed.
     * 
     * @param rootDir The directory containing source files and archives.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    @Override
    public DecompilationResult decompileDirectory(File rootDir, File outputDir) throws DecompilationException
    {
        log.info("Decompiling directory '" + rootDir.getAbsolutePath() + "' to '" + outputDir.getPath());

        DecompilationResult result = new DecompilationResult();
        Path subPath = Paths.get("");
        decompileDirectory(rootDir, outputDir, subPath, result);
        return result;
    }

    public void setExecutorService(ExecutorService service, int numberOfThreads)
    {
        this.exService.shutdown();
        try
        {
            exService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Was not able to decompile in the given time limit.");
        }
        this.numberOfThreads = numberOfThreads;
        this.exService = service;
    }

    private void decompileDirectory(final File rootDir, File outputDir, Path subPath, final DecompilationResult result)
                throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir, "Directory to decompile");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling subdir '" + subPath + "'");

        /*
         * This forces an initialization of the settings.
         */
        getDefaultSettings(outputDir);

        // TODO: Rewrite with Commons IO's DirectoryWalker.
        File curDirFull = rootDir.toPath().resolve(subPath).toFile();
        final List<File> files = Arrays.asList(curDirFull.listFiles());
        Collection<Callable<File>> tasks = new ArrayList<Callable<File>>();
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
                        outputFile = decompileType(metadataSystem, fqcn);
                        if (null == outputFile)
                            throw new IllegalStateException("Unknown Procyon error, type not found.");
                        result.addDecompiled(fileAbsolutePath, outputFile.getAbsolutePath());
                        return outputFile;
                    }
                    catch (Exception e)
                    {
                        DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                                    + rootDir.getPath() + " / " + fileSubPath + ":\n    " + e.getMessage(),
                                    fileSubPath.toString(), e);
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
            exService.invokeAll(tasks);
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
    public DecompilationResult decompileArchive(File archive, File outputDir, DecompilationListener listener) throws DecompilationException
    {
        return decompileArchive(archive, outputDir, null, listener);
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
    public DecompilationResult decompileArchive(final File archive, File outputDir, Filter<ZipEntry> filter, final DecompilationListener listener)
                throws DecompilationException
    {
        Checks.checkFileToBeRead(archive, "Archive to decompile");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling archive '" + archive.getAbsolutePath() + "' to '" + outputDir.getAbsolutePath() + "'");

        JarFile jar = loadJar(archive);
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
            final DecompilerSettings settings = getDefaultSettings(outputDir);
            settings.setTypeLoader(new CompositeTypeLoader(new WindupJarTypeLoader(jar), settings.getTypeLoader()));

            final DecompilationResult res = new DecompilationResult();

            Filter.Result filterRes = Filter.Result.ACCEPT;

            final AtomicInteger current = new AtomicInteger(0);
            final Enumeration<JarEntry> entries = jar.entries();
            Collection<Callable<File>> tasks = new ArrayList<Callable<File>>();

            final Queue<MetadataSystem> metadataSystemCache = new LinkedList<>();
            refreshMetadataCache(metadataSystemCache, settings);

            while (entries.hasMoreElements())
            {
                final JarEntry entry = entries.nextElement();

                if (filter != null)
                    filterRes = filter.decide(entry);
                if (filterRes == Filter.Result.REJECT)
                {
                    jarEntryCount.decrementAndGet();
                    continue;
                }
                if (filterRes == Filter.Result.STOP)
                    break;

                final String name = entry.getName();

                if (!name.endsWith(".class"))
                {
                    jarEntryCount.decrementAndGet();
                    continue;
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
                            final DecompileExecutor t = new DecompileExecutor(metadataSystem, typeName);
                            // TODO - This approach is a hack, but it should work around the Procyon decompiler hangs
                            // for now
                            t.start();
                            t.join(60000L); // wait up to one minute
                            if (!t.success)
                            {
                                if (t.e == null)
                                {
                                    t.cancelDecompilation();
                                    throw new RuntimeException("Failed to compile within one minute... attempting abort", t.e);
                                }
                                else
                                {
                                    throw new RuntimeException(t.e);
                                }
                            }

                            File outputFile = t.outputFile;
                            if (outputFile != null)
                            {
                                listener.fileDecompiled(name, outputFile.getAbsolutePath());
                                res.addDecompiled(name, outputFile.getAbsolutePath());
                            }
                            return outputFile;
                        }
                        catch (Throwable th)
                        {
                            String msg = "Error during decompilation of " + archive.getPath() + "!" + name + ":\n    "
                                        + th.getMessage();
                            DecompilationFailure ex = new DecompilationFailure(msg, name, th);
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
                exService.invokeAll(tasks);
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
     * 
     * TODO: This should be replaced with a more reasonable approach that uses an LRU cache to retain frequently used
     * type resolutions.
     */
    private void refreshMetadataCache(final Queue<MetadataSystem> metadataSystemCache, final DecompilerSettings settings)
    {
        metadataSystemCache.clear();
        for (int i = 0; i < this.numberOfThreads; i++)
        {
            metadataSystemCache.add(new NoRetryMetadataSystem(settings.getTypeLoader()));
        }
    }

    private class DecompileExecutor extends Thread
    {
        private MetadataSystem metadataSystem;
        private String typeName;
        private Exception e;
        private File outputFile;
        private boolean success;

        public DecompileExecutor(MetadataSystem metadataSystem, String typeName)
        {
            this.metadataSystem = metadataSystem;
            this.typeName = typeName;
            setDaemon(true);
        }

        @Override
        public void run()
        {
            try
            {
                this.outputFile = decompileType(metadataSystem, typeName);
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
    private File decompileType(final MetadataSystem metadataSystem, final String typeName) throws IOException
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

        DecompilerSettings settings = this.procyonConf.getDecompilerSettings();
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
        DecompilerSettings settings = this.procyonConf.getDecompilerSettings();
        if (settings == null)
        {
            settings = new DecompilerSettings();
            procyonConf.setDecompilerSettings(settings);
        }
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
        // FIXME This needs to accept generic ZIP archives if possible...
        final JarFile jar;
        try
        {
            jar = new JarFile(archive);
        }
        catch (IOException ex)
        {
            throw new DecompilationException("Can't load .jar: " + archive.getPath(), ex);
        }
        return jar;
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
