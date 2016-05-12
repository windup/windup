package org.jboss.windup.rules.apps.java.archives.identify;

import java.io.File;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.maven.nexusindexer.client.DocTo;
import org.jboss.windup.util.Logging;

/**
 * Identifies archives by their hash, using pre-created Lucene index. See the nexus-repository-indexer project.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 *
 * TODO: This should be in Nexus Indexer - Data (client for Nexus Indexer - Core).
 */
public class LuceneArchiveIdentificationService extends org.jboss.windup.maven.nexusindexer.client.LuceneIndexServiceBase implements ArchiveIdentificationService
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
    public Coordinate getCoordinate(String sha1)
    {
        return this.findSingle(DocTo.Fields.SHA1, sha1, new DocTo<Coordinate>()
        {
            public Coordinate convert(Document doc)
            {
                return CoordinateBuilder.create()
                    .setGroupId(doc.get(GROUP_ID))
                    .setArtifactId(doc.get(ARTIFACT_ID))
                    .setVersion(doc.get(VERSION))
                    .setClassifier(doc.get(CLASSIFIER))
                    .setPackaging(doc.get(PACKAGING));
            }
        });
    }
}
