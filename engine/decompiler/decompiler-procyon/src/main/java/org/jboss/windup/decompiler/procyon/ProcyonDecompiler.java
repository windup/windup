package org.jboss.windup.decompiler.procyon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationFailure;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strobel.assembler.metadata.ClasspathTypeLoader;
import com.strobel.assembler.metadata.CompositeTypeLoader;
import com.strobel.assembler.metadata.IMetadataResolver;
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

/**
 * Decompiles Java classes with Procyon Decompiler. See https://bitbucket.org/mstrobel/procyon
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ProcyonDecompiler implements Decompiler
{
    private static final Logger log = LoggerFactory.getLogger(ProcyonDecompiler.class);
    private final ProcyonConfiguration configuration;

    public ProcyonDecompiler()
    {
        this.configuration = new ProcyonConfiguration();
    }

    public ProcyonDecompiler(ProcyonConfiguration configuration)
    {
        if (configuration == null)
            throw new IllegalArgumentException("Configuration must not be null.");

        this.configuration = configuration;
    }

    @Override
    public DecompilationResult decompileClassFile(File classFile, File outputDir) throws DecompilationException
    {
        if (classFile == null)
            throw new IllegalArgumentException("Class file must not be null.");
        if (!classFile.exists())
            throw new IllegalArgumentException("Class file must exist: " + classFile.getAbsolutePath());
        if (outputDir == null)
            throw new IllegalArgumentException("Output directory must not be null.");
        if (outputDir.exists() && !outputDir.isDirectory())
            throw new IllegalArgumentException("Output directory is a file, expected a directory: "
                        + outputDir.getAbsolutePath());

        log.info("Decompiling .class '" + classFile.getAbsolutePath() + "' to '" + outputDir.getAbsolutePath());

        String name = classFile.getName();
        final String typeName = StringUtils.removeEnd(name, ".class");

        DecompilationResult res = new DecompilationResult();
        try
        {
            DecompilerSettings settings = getDefaultSettings(outputDir);
            MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            this.decompileType(metadataSystem, typeName);
            res.addDecompiled(name);
        }
        catch (Throwable e)
        {
            DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                        + classFile.getAbsolutePath() + ":\n    " + e.getMessage(), name, e);
            log.error(failure.getMessage());
            res.addFailure(failure);
        }

        return res;
    }

    @Override
    public DecompilationResult decompileDirectory(File classesDir, File outputDir) throws DecompilationException
    {
        if (classesDir == null)
            throw new IllegalArgumentException("Directory to decompile must not be null.");
        if (!classesDir.exists())
            throw new IllegalArgumentException("Directory to decompile does not exist: " + classesDir.getPath());
        if (!classesDir.isDirectory())
            throw new IllegalArgumentException("Directory to decompile is a file, expected a directory: "
                        + classesDir.getPath());
        if (outputDir == null)
            throw new IllegalArgumentException("Output directory must not be null.");
        if (outputDir.exists() && !outputDir.isDirectory())
            throw new IllegalArgumentException("Output directory is a file, expected a directory: "
                        + outputDir.getAbsolutePath());

        log.info("Decompiling directory '" + classesDir.getAbsolutePath() + "' to '" + outputDir.getAbsolutePath());

        DecompilerSettings settings = getDefaultSettings(outputDir);
        MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());

        DecompilationResult result = new DecompilationResult();

        final List<File> files = Arrays.asList(classesDir.listFiles());
        for (File file : files)
        {
            if (file.isDirectory())
            {
                DecompilationResult intermediateResult = decompileDirectory(file, new File(outputDir, file.getName()));
                for (String decompiled : intermediateResult.getDecompiled())
                {
                    result.addDecompiled(decompiled);
                }
                for (DecompilationFailure failure : intermediateResult.getFailures())
                {
                    result.addFailure(failure);
                }
                continue;
            }

            String name = file.getName();

            if (!name.endsWith(".class"))
                continue;

            final String typeName = StringUtils.removeEnd(name, ".class");

            try
            {
                this.decompileType(metadataSystem, typeName);
                result.addDecompiled(name);
            }
            catch (Throwable e)
            {
                DecompilationFailure failure = new DecompilationFailure("Error during decompilation of "
                            + classesDir.getPath() + "!" + name + ":\n    " + e.getMessage(), name, e);
                log.error(failure.getMessage(), failure);
                result.addFailure(failure);
            }
        }

        return result;
    }

    @Override
    public DecompilationResult decompileArchive(File archive, File outputDir) throws DecompilationException
    {
        if (archive == null)
            throw new IllegalArgumentException("Archive to decompile must not be null.");
        if (!archive.exists())
            throw new IllegalArgumentException("Archive to decompile does not exist: " + archive.getPath());
        if (archive.isDirectory())
            throw new IllegalArgumentException("Archive to decompile is a directory, expected a file: "
                        + archive.getPath());
        if (outputDir == null)
            throw new IllegalArgumentException("Output directory must not be null.");
        if (outputDir.exists() && !outputDir.isDirectory())
            throw new IllegalArgumentException("Output directory is a file, expected a directory: "
                        + outputDir.getAbsolutePath());

        log.info("Decompiling archive '" + archive.getAbsolutePath() + "' to '" + outputDir.getAbsolutePath());

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

        DecompilerSettings settings = getDefaultSettings(outputDir);

        settings.setTypeLoader(new CompositeTypeLoader(new JarTypeLoader(jar), settings.getTypeLoader()));

        MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());

        int classesDecompiled = 0;
        DecompilationResult res = new DecompilationResult();

        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();

            if (!name.endsWith(".class"))
                continue;

            final String typeName = StringUtils.removeEnd(name, ".class");

            try
            {
                File outputFile = this.decompileType(metadataSystem, typeName);
                if (outputFile != null)
                {
                    res.addDecompiled(name);
                    res.addDecompiledOutputFile(outputFile.getAbsolutePath());
                }

                // Taken from mstrobel's, not sure what's the purpose.
                if (++classesDecompiled % 100 == 0)
                    metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            }
            catch (Throwable th)
            {
                String msg = "Error during decompilation of " + archive.getPath() + "!" + name + ":\n    "
                            + th.getMessage();
                DecompilationFailure ex = new DecompilationFailure(msg, name, th);
                log.error(msg, ex);
                res.addFailure(ex);
            }
        }

        return res;
    }

    private File decompileType(final MetadataSystem metadataSystem, final String typeName) throws IOException
    {
        log.debug("Decompiling " + typeName);

        final TypeReference type;

        // Hack to get around classes whose descriptors clash with primitive types.
        if (typeName.length() == 1)
        {
            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(typeName);

            type = metadataSystem.resolve(reference);
        }
        else
        {
            type = metadataSystem.lookupType(typeName);
        }

        if (type == null)
        {
            log.error("Failed to load class: %s", typeName);
            return null;
        }

        final TypeDefinition resolvedType = type.resolve();
        if (resolvedType == null)
        {
            log.error("Failed to load class: %s", typeName);
            return null;
        }

        if (!configuration.isIncludeNested()
                    && (resolvedType.isNested() || resolvedType.isAnonymous() || resolvedType.isSynthetic()))
        {
            return null;
        }

        DecompilerSettings settings = configuration.getDecompilerSettings();
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

        if (!configuration.getLineNumberOptions().isEmpty())
        {

            final LineNumberFormatter lineFormatter = new LineNumberFormatter(writer.getFile(), lineNumberPositions,
                        configuration.getLineNumberOptions());

            lineFormatter.reformatFile();
        }
        return writer.getFile();
    }

    private DecompilerSettings getDefaultSettings(File outputDir)
    {
        DecompilerSettings settings = configuration.getDecompilerSettings();
        if (settings == null)
            settings = new DecompilerSettings();
        settings.setOutputDirectory(outputDir.getPath());
        settings.setShowSyntheticMembers(false);

        if (settings.getTypeLoader() == null)
            settings.setTypeLoader(new ClasspathTypeLoader());
        return settings;
    }

    /**
     * Helper method which validates the files etc.
     */
    private FileOutputWriter createFileWriter(final TypeDefinition type, final DecompilerSettings settings)
                throws IOException
    {
        final String outputDirectory = settings.getOutputDirectory();

        final String fileName = type.getName() + settings.getLanguage().getFileExtension();
        final String packageName = type.getPackageName();

        final String subDir = StringUtils.defaultIfBlank(packageName, "").replace('.', File.separatorChar);
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
