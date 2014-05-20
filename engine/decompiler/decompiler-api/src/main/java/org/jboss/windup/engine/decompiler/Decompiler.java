package org.jboss.windup.engine.decompiler;

import java.io.File;

/**
 * Used to decompile Java .class files and archives.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Decompiler
{
    /**
     * Decompiles the given .class file and creates the specified output source file.
     * 
     * @param classFile the .class file to be decompiled.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    public DecompilationResult decompileClassFile(File classFile, File outputDir) throws DecompilationException;

    /**
     * Decompiles all .class files and archives in the given directory and places results in the specified output
     * directory.
     * <p>
     * Discovered archives will be decompiled into directories matching the name of the archive, e.g.
     * <code>foo.ear/bar.jar/src/com/foo/bar/Baz.java</code>.
     * <p>
     * Required directories will be created as needed.
     * 
     * @param classesDir The directory containing source files and archives.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    public DecompilationResult decompileDirectory(File classesDir, File outputDir) throws DecompilationException;

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
     */
    public DecompilationResult decompileArchive(File archive, File outputDir) throws DecompilationException;
}
