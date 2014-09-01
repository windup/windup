package org.jboss.windup.decompiler.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;


/**
 * Interface extracted from Common IO DirectoryWalker.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface WalkerCallback<T>
{
    void handleStart(File startDirectory, Collection<T> results) throws IOException;
    
    boolean handleDirectory(Path dir, int depth, Collection<T> results) throws IOException;
    void handleDirectoryStart(Path directory, int depth, Collection<T> results) throws IOException;
    File[] filterDirectoryContents(Path directory, int depth, File[] files) throws IOException;
    void handleDirectoryEnd(Path directory, int depth, Collection<T> results) throws IOException;

    void handleFile(Path file, int depth, Collection<T> results) throws IOException;

    void handleEnd(Collection<T> results) throws IOException;
}
