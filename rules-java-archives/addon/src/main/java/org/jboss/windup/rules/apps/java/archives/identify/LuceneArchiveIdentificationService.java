package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 * Identifies archives by their hash, using pre-created Lucene index. See the nexus-repository-indexer project.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 *
 * TODO: This should be in Nexus Indexer - Data (client for Nexus Indexer - Core).
 */
public class LuceneArchiveIdentificationService extends LuceneIndexServiceBase implements ArchiveIdentificationService
{
    private static final Logger LOG = Logging.get(LuceneArchiveIdentificationService.class);

    private static final String SHA1 = "sha1";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String PACKAGING = "packaging";
    private static final String CLASSIFIER = "classifier";
    private static final String VERSION = "version";


    public LuceneArchiveIdentificationService(File directory)
    {
        super(directory);
    }


    @Override
    public Coordinate getCoordinate(String checksum)
    {
        Query query = new TermQuery(new Term(SHA1, checksum));
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

                Coordinate coordinate = CoordinateBuilder.create()
                    .setGroupId(groupId).setArtifactId(artifactId).setVersion(version)
                    .setClassifier(classifier).setPackaging(packaging);
                return coordinate;
            }
            return null;
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to find Maven coords for SHA1: " + checksum + " due to: " + e.getMessage(), e);
        }
    }
}
