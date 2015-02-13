package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains information from the META-INF/MANIFEST.MF file within an archive.
 */
@TypeValue(JarManifestModel.TYPE)
public interface JarManifestModel extends FileModel, SourceFileModel
{
    public static final String TYPE = "JarManifestModel";
    public static final String ARCHIVE = "archive";

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    public void setArchive(final ArchiveModel archive);

    @Adjacency(label = ARCHIVE, direction = Direction.IN)
    public ArchiveModel getArchive();
}
