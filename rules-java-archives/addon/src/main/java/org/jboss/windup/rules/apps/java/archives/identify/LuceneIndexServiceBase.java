package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.jboss.windup.util.Checks;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public abstract class LuceneIndexServiceBase implements Closeable
{
    private static final Logger LOG = Logging.get(LuceneIndexServiceBase.class);

    protected File directory;
    protected Directory index;
    protected IndexReader reader;
    protected IndexSearcher searcher;


    public LuceneIndexServiceBase(File directory)
    {
        Checks.checkDirectoryToBeRead(directory, "Lucene index directory");

        this.directory = directory;
        try
        {
            initialize();
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to load Lucene index due to: " + e.getMessage(), e);
        }
    }


    private void initialize() throws IOException
    {
        this.index = new SimpleFSDirectory(this.directory);
        this.reader = DirectoryReader.open(index);
        this.searcher = new IndexSearcher(reader);
    }


    @Override
    public final void close()
    {
        try
        {
            this.reader.close();
            this.index.close();
        }
        catch (Exception e)
        {
            LOG.warning("Failed to close lucene index at: " + this.directory + " due to: " + e.getMessage());
        }
    }


    public final IndexSearcher getSearcher()
    {
        return searcher;
    }

    public final File getDirectory()
    {
        return directory;
    }

}
