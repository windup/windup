package org.jboss.windup.decompiler.api;

import java.nio.file.Path;
import java.util.zip.ZipEntry;

import org.jboss.windup.decompiler.util.Filter;

/**
 * Used to decompile Java .class files and archives.
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface Decompiler
{
    /**
     * Decompiles the given .class file and creates the specified output source file in the given output dir under appropriate package subdirectories,
     * like $outputDir/org/jboss/Foo.java. Decompilation may need multiple .class files for one .java file, e.g. for inner classes.
     * 
     * @param classFile the .class file to be decompiled.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    public DecompilationResult decompileClassFile(Path rootDir, Path classFilePath, Path outputDir)
                throws DecompilationException;

    /**
     * Close all the resources
     */
    public void close();

    /**
     * Decompiles all .class files and archives in the given directory and places results in the specified output directory.
     * <p>
     * Discovered archives will be decompiled into directories matching the name of the archive, e.g.
     * <code>foo.ear/bar.jar/src/com/foo/bar/Baz.java</code>.
     * <p>
     * Required directories will be created as needed.
     * 
     * @param classesDir The directory containing source files and archives.
     * @param outputDir The directory where decompiled .java files will be placed.
     */
    public DecompilationResult decompileDirectory(Path classesDir, Path outputDir) throws DecompilationException;

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
     * @param listener This is called after each successful decompilation
     */
    public DecompilationResult decompileArchive(Path archive, Path outputDir, DecompilationListener listener) throws DecompilationException;

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
     * @param filter Decides what files from the archive to decompile.
     * @param listener This is called after each successful decompilation
     */
    public DecompilationResult decompileArchive(Path archive, Path outputDir, Filter<ZipEntry> filter, DecompilationListener listener)
                throws DecompilationException;
}
