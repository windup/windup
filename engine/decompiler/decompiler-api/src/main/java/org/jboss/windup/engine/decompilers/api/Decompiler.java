package org.jboss.windup.engine.decompilers.api;

import java.io.File;

/**
 * A set of interfaces to decompile .class files, archives, or directories with mix of those.
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface Decompiler {
    
    
    public static interface Conf<T extends DecompilationConf> {
        
        public T retypeConf( DecompilationConf conf );
        
    }

    
    public static interface Type {
        /**
         * Decompiles given .class file.
         * 
         * @param srcDir
         * @param destDir  Where to put the decompiled .java files.
         */
        public void decompileClassFile( File srcClassFile, File destFile, DecompilationConf conf ) throws DecompilationException;
    }

    public static interface Jar {
        /**
         * Decompiles all .class files in the given archive.
         * 
         * @param srcDir
         * @param destDir  Where to put the decompiled .java files.
         *      Sources from archives will be put into the same named directories, e.g.
         *          foo.ear/bar.jar/src/com/foo/bar/Baz.java
         */
        public JarDecompilationResults decompileJar( File srcJar, File destDir, DecompilationConf conf  ) throws DecompilationException;
    }
    
    public static interface Dir {
        /**
         * Decompiles whole directory - content of all .class files and all known archives.
         * 
         * @param srcDir
         * @param destDir  Where to put the decompiled .java files.
         *      Sources from archives will be put into the same named directories, e.g.
         *          foo.ear/bar.jar/src/com/foo/bar/Baz.java
         */
        public void decompileDir( File srcDir, File destDir, DecompilationConf conf ) throws DecompilationException;
        
    }
    
}
