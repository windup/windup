package org.jboss.windup.rules.apps.java.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Contains links to {@link JarManifestModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(HasManifestFilesModel.TYPE)
public interface HasManifestFilesModel extends WindupVertexFrame
{
    String TYPE = "HasManifestFilesModel";

    /**
     * Contains links to the manifest models associated with this vertex.
     */
    @Adjacency(label = JarManifestModel.ARCHIVE, direction = Direction.OUT)
    Iterable<JarManifestModel> getManifestModels();

    /**
     * Contains links to the manifest models associated with this vertex.
     */
    @Adjacency(label = JarManifestModel.ARCHIVE, direction = Direction.OUT)
    void addManifestModel(final JarManifestModel archive);
}
