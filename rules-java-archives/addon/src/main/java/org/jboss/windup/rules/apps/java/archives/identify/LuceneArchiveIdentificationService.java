package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 * Identifies archives by their hash, using pre-created Lucene index. See the nexus-repository-indexer project.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class LuceneArchiveIdentificationService implements ArchiveIdentificationService, Closeable
{
    private static final Logger LOG = Logging.get(LuceneArchiveIdentificationService.class);

    private static final String SHA1 = "sha1";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String PACKAGING = "packaging";
    private static final String CLASSIFIER = "classifier";
    private static final String VERSION = "version";

    private File directory;
    private Directory index;
    private IndexReader reader;
    private IndexSearcher searcher;

    public LuceneArchiveIdentificationService(File directory)
    {
        Assert.isTrue(directory.exists(), "Hash to Lucene index directory does not exist: " + directory.toString());

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

    public void close() {
        try {
            this.reader.close();
            this.index.close();
        } catch (Exception e) {
            LOG.warning("Failed to close lucene index at: " + this.directory + " due to: " + e.getMessage());
        }
    }

    @Override
    public Coordinate getCoordinate(String checksum)
    {

        Query query = new TermQuery(new Term("sha1", checksum));
        try
        {
            TopDocs results = searcher.search(query, 100);
            for (ScoreDoc scoreDoc : results.scoreDocs)
            {
                Document doc = searcher.doc(scoreDoc.doc);
                String groupId = doc.get(GROUP_ID);
                String artifactId = doc.get(ARTIFACT_ID);
                String version = doc.get(VERSION);
                String classifier = doc.get(CLASSIFIER);
                String packaging = doc.get(PACKAGING);

                Coordinate coordinate = CoordinateBuilder.create().setGroupId(groupId).setArtifactId(artifactId).setVersion(version)
                            .setClassifier(classifier).setPackaging(packaging);
                return coordinate;
            }
            return null;
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to find checksum: " + checksum + " due to: " + e.getMessage(), e);
        }
    }
}
