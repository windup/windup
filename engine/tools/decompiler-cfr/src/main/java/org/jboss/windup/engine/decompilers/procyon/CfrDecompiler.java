package org.jboss.windup.engine.decompilers.procyon;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.decompilers.api.DecompilationConf;
import org.jboss.windup.engine.decompilers.api.DecompilationEx;
import org.jboss.windup.engine.decompilers.api.DecompilationPathEx;
import org.jboss.windup.engine.decompilers.api.IDecompiler;
import org.jboss.windup.engine.decompilers.api.JarDecompilationResults;
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
public class CfrDecompiler implements IDecompiler.Conf<CfrConf>, IDecompiler.Jar {
    private static final Logger log = LoggerFactory.getLogger( CfrDecompiler.class );
    
    
    /**
     *  Decompiles a single .class file.
     * @param metadataSystem
     * @param typeName  Name of the type. Looked up through the MetadataSystem.
     * @param conf_   Configuration specific for this decompiler.
     */
    public void decompileType(
            final String typeName,
            final DecompilationConf conf_) throws IOException
    {
        log.debug("Decompiling " + typeName);
        
        CfrConf conf = this.retypeConf( conf_ );


    }    
    
    
    /**
     *  Extracts the archive and decompiles all .class files found.
     */
    @Override
    public JarDecompilationResults decompileJar( File jarFile, File destDir, DecompilationConf conf_ ) throws DecompilationEx {
        
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
        CfrConf conf = this.retypeConf( conf_ );

        
        int classesDecompiled = 0;
        //List<Throwable> exs = new LinkedList<>();
        JarDecompilationResults res = new JarDecompilationResults();

        // For each entry in the archive...
        final Enumeration<JarEntry> entries = jar.entries();
        while( entries.hasMoreElements() ) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();

            if( ! name.endsWith(".class") )
                continue;

            final String typeName = StringUtils.removeEnd(name, ".class");

            try {
                
            }
            catch(Throwable th) {
                String msg = "Error during decompilation of " + jarFile.getPath() + "!" + name + ":\n    " + th.getMessage();
                DecompilationPathEx ex = new DecompilationPathEx( msg, name, th );
                log.error(msg, ex);
                res.addFailed( ex );
            }
            // Throw a compound exception?
        }
        
        return res;
    }// decompileJar
    
    
    
    



    /**
     *  Convenience - check the type and retypes the conf parameter.
     *  It could also validate.
     */
    @Override
    public CfrConf retypeConf( DecompilationConf conf ) {
        if( conf instanceof CfrConf )
            return (CfrConf) conf;
        throw new IllegalArgumentException( String.format("Configuration for % has to be %s, was %s",
            CfrDecompiler.class.getSimpleName(),
            CfrConf.class.getSimpleName(), conf.getClass().getName()
        ));
    }
    

}// class
