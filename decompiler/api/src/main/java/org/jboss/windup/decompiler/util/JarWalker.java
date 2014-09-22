package org.jboss.windup.decompiler.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class JarWalker implements Walker {

    private File archive;
    private IOFileFilter filter;


    public JarWalker(File archive, IOFileFilter filter)
    {
        this.archive = archive;
        this.filter = filter;
    }

    
    /**
     * Walks the .jar of this JarWalker, calling the given callback.
     * 
     * @param res The collection will be passed to the callback methods to put the results in.
     */
    @Override
    public Collection walk(WalkerCallback callback, Collection res) throws IllegalArgumentException, IOException
    {
        JarFile jar = loadJar(this.archive);
        callback.handleStart(this.archive, res);
        
        // MetadataSystem, TypeLoader's
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            final Path path = Paths.get(name);
            int depth = StringUtils.countMatches(name, "/");
            
            if( ! filter.accept(new File(name)) ){
                continue;
            }
            if( entry.isDirectory() ){
                callback.handleDirectory(path, depth, res);
            }
            else
                callback.handleFile(path, depth, res);
        }
        
        return res;
    }
    

    /**
     * Opens the jar, wraps any IOException.
     */
    private JarFile loadJar(File archive) throws IOException
    {
        // FIXME This needs to accept generic ZIP archives if possible...
        final JarFile jar;
        try
        {
            jar = new JarFile(archive);
        }
        catch (IOException ex)
        {
            throw new IOException("Can't load .jar: " + archive.getPath(), ex);
        }
        return jar;
    }


}// class
