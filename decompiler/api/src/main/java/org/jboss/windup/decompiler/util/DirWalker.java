package org.jboss.windup.decompiler.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.DirectoryWalker;

/**
 * Implementation of Walker for walking a file system directory.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class DirWalker<T> implements Walker<T> {

    private File startDir;
    private FileFilter filter;


    public DirWalker(File startDir, FileFilter filter)
    {
        this.startDir = startDir;
        this.filter = filter;
    }
    
    

    @Override
    public Collection walk(WalkerCallback<T> callback, Collection<T> res) throws IllegalArgumentException, IOException
    {
        new WalkerDirWalker(filter, callback).go(startDir, res);
        return res;
    }
    
    class WalkerDirWalker<T> extends DirectoryWalker<T> {
        
        WalkerCallback<T> callback;


        public WalkerDirWalker(FileFilter filter, WalkerCallback<T> callback)
        {
            super(filter, -1);
            this.callback = callback;
        }
        
        
        public void go(File startDir, Collection<T> res) throws IOException {
            super.walk(startDir, res);
        }
        

        @Override
        protected void handleStart(File startDir, Collection<T> res) throws IOException
        {
            callback.handleStart(startDir, res);
        }

        @Override
        protected void handleEnd(Collection<T> res) throws IOException
        {
            callback.handleEnd(res);
        }


        @Override
        protected void handleDirectoryStart(File dir, int depth, Collection<T> res) throws IOException
        {
            callback.handleDirectoryStart(dir.toPath(), depth, res);
        }


        @Override
        protected boolean handleDirectory(File dir, int depth, Collection<T> res) throws IOException
        {
            return callback.handleDirectory(dir.toPath(), depth, res);
        }

        @Override
        protected File[] filterDirectoryContents(File dir, int depth, File[] files) throws IOException
        {
            return callback.filterDirectoryContents(dir.toPath(), depth, files);
        }
        
        @Override
        protected void handleDirectoryEnd(File dir, int depth, Collection<T> res) throws IOException
        {
            callback.handleDirectoryEnd(dir.toPath(), depth, res);
        }


        @Override
        protected void handleFile(File file, int depth, Collection<T> res) throws IOException
        {
            callback.handleFile(file.toPath(), depth, res);
        }

    }

}// class
