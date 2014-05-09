package org.jboss.windup.engine.decompilers.procyon;

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
import com.strobel.decompiler.LineNumberFormatter;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.BytecodeLanguage;
import com.strobel.decompiler.languages.LineNumberPosition;
import com.strobel.decompiler.languages.TypeDecompilationResults;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.io.PathHelper;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.decompilers.api.DecompilationConf;
import org.jboss.windup.engine.decompilers.api.DecompilationEx;
import org.jboss.windup.engine.decompilers.api.IDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Decompiles Java classes with Procyon Decompiler.
 *  See https://bitbucket.org/mstrobel/procyon
 * 
 * This is basically the DecompilerDriver, refactored - I've removed things like 
 *   use of the default charset,
 *   output to stdout,
 *   unused DecompilerOptions (which only carried DecompilerSettings),
 *   logging to stdout,
 *   arbitrary StringUtils,
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ProcyonDecompiler implements IDecompiler.Conf<ProcyonConf>, IDecompiler.Jar {
    private static final Logger log = LoggerFactory.getLogger( ProcyonDecompiler.class );
    
    
    /**
     *  Decompiles a single .class file.
     * @param metadataSystem
     * @param typeName  Name of the type. Looked up through the MetadataSystem.
     * @param conf_   Configuration specific for this decompiler.
     */
    public void decompileType(
            final MetadataSystem metadataSystem,
            final String typeName,
            final DecompilationConf conf_) throws IOException
    {
        log.debug("Decompiling " + typeName);
        
        ProcyonConf conf = this.retypeConf( conf_ );

        final TypeReference type;

        // Hack to get around classes whose descriptors clash with primitive types.
        if (typeName.length() == 1) {
            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(typeName);

            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(typeName);
        }
        
        if( type == null ){
            log.error("Failed to load class: %s", typeName);
            return; /// ??
        }

        final TypeDefinition resolvedType = type.resolve();
        if( resolvedType == null ){
            log.error("Failed to load class: %s", typeName);
            return; /// ??
        }

        if( ! conf.isIncludeNested() && (resolvedType.isNested() || resolvedType.isAnonymous() || resolvedType.isSynthetic()) ){
            return;
        }
        
        
        // Get settings.
        DecompilerSettings settings = conf.getDecompilerSettings();
        settings.setFormattingOptions( new JavaFormattingOptions() );

        final FileOutputWriter writer = createFileWriter(resolvedType, settings);
        final PlainTextOutput output;

        output = new PlainTextOutput(writer);
        output.setUnicodeOutputEnabled(settings.isUnicodeOutputEnabled());
        if (settings.getLanguage() instanceof BytecodeLanguage)
            output.setIndentToken("  ");

        // Create DecompilationOptions
        DecompilationOptions options = new DecompilationOptions();
        options.setSettings( settings );  // I'm missing why these two classes are split.
        
        // --------- DECOMPILE ---------
        final TypeDecompilationResults results = settings.getLanguage().decompileType(resolvedType, output, options);
        // TODO: Multi-threaded.

        writer.flush();
        writer.close();

        // If we're writing to a file and we were asked to include line numbers in any way,
        // then reformat the file to include that line number information.
        final List<LineNumberPosition> lineNumberPositions = results.getLineNumberPositions();

        if( ! conf.getLineNumberOptions().isEmpty() ) {

            final LineNumberFormatter lineFormatter = new LineNumberFormatter(
                writer.getFile(),
                lineNumberPositions,
                conf.getLineNumberOptions()
            );

            lineFormatter.reformatFile();
        }
    }    
    
    
    /**
     *  Extracts the archive and decompiles all .class files found.
     */
    @Override
    public void decompileJar( File jarFile, File destDir, DecompilationConf conf ) throws DecompilationEx {
        
        log.info("Decompiling .jar '" + jarFile.getPath() + "' to '" + destDir + "'...");
        
        
        // Verify input.
        if( jarFile == null )
            throw new DecompilationEx("Param jarFile is null.");
        if( destDir == null )
            throw new DecompilationEx("Param destDir is null.");
        if( ! jarFile.exists() )
            throw new DecompilationEx(".jar file not found: " + jarFile.getPath());
        if( destDir.exists() && ! destDir.isDirectory() )
            throw new DecompilationEx("Destination path is not a directory: " + destDir.getAbsolutePath() );
            
        // Load the .jar
        final JarFile jar;
        try {
            jar = new JarFile(jarFile);
        } catch( IOException ex ) {
            throw new DecompilationEx("Can't load .jar: " + jarFile.getPath(), ex);
        }

        // Settings
        final DecompilerSettings settings = new DecompilerSettings();
        settings.setOutputDirectory( destDir.getPath() );
        settings.setShowSyntheticMembers(false);
        
        // Add ClasspathTypeLoader if nothing is set yet.
        if( settings.getTypeLoader() == null )
            // java.class.path + sun.boot.class.path
            settings.setTypeLoader( new ClasspathTypeLoader() );

        // Add JarTypeLoader to the set of loaders.
        settings.setTypeLoader(
            new CompositeTypeLoader(
                new JarTypeLoader(jar),
                settings.getTypeLoader()
            )
        );

        // NoRetry keeps list of failed types.
        MetadataSystem metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
        //metadataSystem.setEagerMethodLoadingEnabled();
        
        int classesDecompiled = 0;
        List<Throwable> exs = new LinkedList<>();

        // For each entry in the archive...
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();

            if( ! name.endsWith(".class") )
                continue;

            final String typeName = StringUtils.removeEnd(name, ".class");

            try {
                this.decompileType( metadataSystem, typeName, conf );

                // Taken from mstrobel's, not sure what's the reason.
                if (++classesDecompiled % 100 == 0)
                    metadataSystem = new NoRetryMetadataSystem(settings.getTypeLoader());
            }
            catch(Throwable th) {
                log.error("Error during decompilation of " + typeName + ":\n    " + th.getMessage(), th);
                exs.add( th ); // Throw at the end?
            }
        }
    }// decompileJar
    
    
    
    
    /**
     *  Helper method which validates the files etc.
     */
    private static FileOutputWriter createFileWriter(final TypeDefinition type, final DecompilerSettings settings) throws IOException {
        final String outputDirectory = settings.getOutputDirectory();

        final String fileName = type.getName() + settings.getLanguage().getFileExtension();
        final String packageName = type.getPackageName();

        final String subDir = StringUtils.defaultIfBlank(packageName, "").replace('.', File.separatorChar);
        final String outputPath = PathHelper.combine( outputDirectory, subDir, fileName );

        final File outputFile = new File(outputPath);
        final File parentDir = outputFile.getParentFile();

        if( parentDir != null && ! parentDir.exists() && ! parentDir.mkdirs()) {
            throw new IllegalStateException("Could not create directory:" + parentDir);
        }

        if( ! outputFile.exists() && ! outputFile.createNewFile() ) {
            throw new IllegalStateException("Could not create output file: " + outputPath);
        }

        return new FileOutputWriter( outputFile, settings );
    }
    
    


    /**
     *  Convenience - check the type and retypes the conf parameter.
     *  It could also validate.
     */
    @Override
    public ProcyonConf retypeConf( DecompilationConf conf ) {
        if( conf instanceof ProcyonConf )
            return (ProcyonConf) conf;
        throw new IllegalArgumentException( String.format("Configuration for % has to be %s, was %s",
            ProcyonDecompiler.class.getSimpleName(),
            ProcyonConf.class.getSimpleName(), conf.getClass().getName()
        ));
    }
    

}// class
