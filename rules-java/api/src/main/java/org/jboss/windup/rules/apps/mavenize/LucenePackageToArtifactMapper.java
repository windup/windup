package org.jboss.windup.rules.apps.mavenize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.jboss.windup.maven.nexusindexer.client.DocTo;
import org.jboss.windup.util.ZipUtil;

/**
 * Returns the artifacts which are known to contain given package, using pre-created Lucene index.
 * See the nexus-repository-indexer project.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class LucenePackageToArtifactMapper extends org.jboss.windup.maven.nexusindexer.client.LuceneIndexServiceBase implements PackageToArtifactsMapper
{
    public LucenePackageToArtifactMapper(File directory)
    {
        super(directory);
    }


    @Override
    public List<MavenCoord> getArtifactsContainingPackage(String pkg)
    {
        final List<MavenCoord> artifacts = new ArrayList<>(64);
        this.findByField(DocTo.Fields.PACKAGE, pkg, 100, new ZipUtil.Visitor<Document>()
        {
            @Override
            public void visit(Document doc)
            {
                /*MavenCoord coord = new MavenCoord()
                    .setGroupId(doc.get(GROUP_ID))
                    .setArtifactId(doc.get(ARTIFACT_ID))
                    .setVersion(doc.get(VERSION))
                    .setClassifier(doc.get(CLASSIFIER))
                    .setPackaging(doc.get(PACKAGING));*/
                MavenCoord coord = MavenCoord.fromGAVPC(doc.get(DocTo.Fields.COORD_GAVCP));
                artifacts.add(coord);
            }
        });
        return artifacts;
    }
}
