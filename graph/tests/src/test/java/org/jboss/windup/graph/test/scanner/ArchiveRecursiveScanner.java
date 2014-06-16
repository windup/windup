package org.jboss.windup.graph.test.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ArchiveRecursiveScanner
{
    private static final Logger log = LoggerFactory.getLogger(ArchiveRecursiveScanner.class);

    @Inject
    private GraphContext context;
    @Inject
    private GraphUtil graphUtil;

    /**
     * Recursively scans the given archive file.
     */
    public void scanArchive(File ar) throws FileNotFoundException, WindupException
    {
        // Verify input.
        if (ar == null)
            throw new IllegalArgumentException("Param jarFile is null.");
        if (!ar.exists())
            throw new FileNotFoundException(".jar file not found: " + ar.getPath());

        try
        {
            this.scanArchive(new FileInputStream(ar));
        }
        catch (IOException ex)
        {
            throw new WindupException("Can't load .jar: " + ar.getPath(), ex);
        }
    }

    /**
     * Recursively scans the given archive file.
     */
    public void scanArchive(InputStream arIS) throws IOException
    {
        // Load the .jar
        final JarInputStream jar;
        try
        {
            jar = new JarInputStream(arIS);
        }
        catch (IOException ex)
        {
            // throw new WindupException("Can't load .jar: " + jarFile.getPath(), ex);
            throw ex;
        }

        // For each entry...
        for (JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry())
        {
            // TODO: Store to the graph...
            // .addFileNode
        }

    }

}// class

class ScanContext
{
    Deque<ScanStackEntry> scannedArchivesStack = new LinkedList<>();
}

class ScanStackEntry
{
    Path scannedArchivePath;
    File tmpFile;
}
