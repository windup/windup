package org.jboss.windup.decompiler.procyon;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.ClasspathTypeLoader;
import com.strobel.assembler.metadata.CompositeTypeLoader;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.util.Checks;
import org.jboss.windup.decompiler.util.Filter;


/**
 * Decompiles Java classes with Procyon Decompiler. See https://bitbucket.org/mstrobel/procyon
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompiler implements Decompiler
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


    /**
     * Decompiles the given .class file and creates the specified output source file.
     *
     * @param classFilePath the .class file to be decompiled.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    @Override
    public DecompilationResult decompileClassFile(File rootDir, Path classFilePath, File outputDir) throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir, "Classes root dir");
        File classFile = rootDir.toPath().resolve(classFilePath).toFile();
        Checks.checkFileToBeRead(classFile, "Class file");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling .class '" + classFilePath + "' to '" + outputDir.getPath() + "'");

        String name = classFilePath.toString();
        final String typeName = StringUtils.removeEnd(name, ".class");//.replace('/', '.');

        DecompilationResult res = new DecompilationResult();
        try
        {
            DecompilerSettings settings = getDefaultSettings(outputDir);
            this.procyonConf.setDecompilerSettings(settings); // TODO: This is horrible mess.
            
            ITypeLoader typeLoader = new CompositeTypeLoader(new ClasspathTypeLoader(rootDir.getPath()), new ClasspathTypeLoader());
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


    private void decompileDirectory(File rootDir, File outputDir, Path subPath, DecompilationResult result)
            throws DecompilationException
    {
        Checks.checkDirectoryToBeRead(rootDir, "Directory to decompile");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling subdir '" + subPath + "'");

        DecompilerSettings settings = getDefaultSettings(outputDir);
        //MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
        //MetadataSystem metadataSystem = new NoRetryMetadataSystem(rootDir.getPath());
        MetadataSystem metadataSystem = new NoRetryMetadataSystem(new InputTypeLoader());

        // TODO: Rewrite with Commons IO's DirectoryWalker.
        File curDirFull = rootDir.toPath().resolve(subPath).toFile();
        final List<File> files = Arrays.asList(curDirFull.listFiles());
        for (File file : files)
        {
            // Directory...
            if (file.isDirectory()) {
                // Recurse.
                Path subPathNew = subPath.resolve(file.getName());
                decompileDirectory(rootDir, outputDir, subPathNew, result);
                continue;
            }

            // .class ?
            if ( ! file.getName().endsWith(".class"))
                continue;

            // Inner class?
            if(file.getName().contains("$"))
                continue;

            String fileSubPath = subPath.resolve(file.getName()).toString();
            String fqcn = StringUtils.removeEnd(fileSubPath, ".class").replace('/', '.');
            try
            {
                File outputFile = this.decompileType(metadataSystem, fqcn);
                if( null == outputFile )
                    throw new IllegalStateException("Unknown Procyon error, type not found.");
                result.addDecompiled(file.getAbsolutePath(), outputFile.getAbsolutePath());
            }
            catch (Throwable ex)
            {
                DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                            + rootDir.getPath() + " / " + fileSubPath + ":\n    " + ex.getMessage(), fileSubPath.toString(), ex);
                log.log(Level.SEVERE, failure.getMessage(), failure);
                result.addFailure(failure);
            }
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
    public DecompilationResult decompileArchive(File archive, File outputDir) throws DecompilationException
    {
        return decompileArchive(archive, outputDir, null);
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
     * @param filter   Decides which classes will be decompiled.
     *
     * @returns Result with all decompilation failures. Never throws.
     */
    public DecompilationResult decompileArchive(File archive, File outputDir, Filter<ZipEntry> filter) throws DecompilationException
    {
        Checks.checkFileToBeRead(archive, "Archive to decompile");
        Checks.checkDirectoryToBeFilled(outputDir, "Output directory");

        log.info("Decompiling archive '" + archive.getAbsolutePath() + "' to '" + outputDir.getAbsolutePath() + "'");

        JarFile jar = loadJar(archive);

        
        // MetadataSystem, TypeLoader's
        DecompilerSettings settings = getDefaultSettings(outputDir);
        settings.setTypeLoader(new CompositeTypeLoader(new JarTypeLoader(jar), settings.getTypeLoader()));
        MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());

        int classesDecompiled = 0;
        DecompilationResult res = new DecompilationResult();

        Filter.Result filterRes = Filter.Result.ACCEPT;
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            
            if(filter != null)
                filterRes = filter.decide(entry);
            if(filterRes == Filter.Result.REJECT)
                continue;
            if(filterRes == Filter.Result.STOP)
                break;
            
            final String name = entry.getName();

            if (!name.endsWith(".class"))
                continue;

            final String typeName = StringUtils.removeEnd(name, ".class");

            try
            {
                File outputFile = this.decompileType(metadataSystem, typeName);
                if (outputFile != null)
                    res.addDecompiled(name, outputFile.getAbsolutePath());

                // Taken from mstrobel's, not sure what's the purpose.
                if (++classesDecompiled % 100 == 0)
                    metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            }
            catch (Throwable th)
            {
                String msg = "Error during decompilation of " + archive.getPath() + "!" + name + ":\n    " + th.getMessage();
                DecompilationFailure ex = new DecompilationFailure(msg, name, th);
                log.log(Level.SEVERE, msg, ex);
                res.addFailure(ex);
            }
        }

        return res;
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
        if(!this.procyonConf.isIncludeNested() && nested)
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
     *  Default settings set type loader to ClasspathTypeLoader if not set before.
     */
    private DecompilerSettings getDefaultSettings(File outputDir)
    {
        DecompilerSettings settings = this.procyonConf.getDecompilerSettings();
        if (settings == null)
            settings = new DecompilerSettings();
        settings.setOutputDirectory(outputDir.getPath());
        settings.setShowSyntheticMembers(false);

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
    private static FileOutputWriter createFileWriter(final TypeDefinition type, final DecompilerSettings settings)
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
