package org.jboss.windup.rules.apps.java.service;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;

/**
 * Manages the creation, querying, and deletion of {@link JarManifestModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class JarManifestService extends GraphService<JarManifestModel>
{
    public JarManifestService(GraphContext context)
    {
        super(context, JarManifestModel.class);
    }

    /**
     * Gets all {@link JarManifestModel}s associated with this archive.
     */
    public Iterable<JarManifestModel> getManifestsByArchive(ArchiveModel archiveModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(archiveModel.asVertex());
        pipeline.out(JarManifestModel.ARCHIVE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, JarManifestModel.class);
    }
}
