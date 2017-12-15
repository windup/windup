package org.jboss.windup.rules.apps.javaee.service;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;

/**
 * Contains methods for querying, creating, and deleting {@link HibernateEntityModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class HibernateEntityService extends GraphService<HibernateEntityModel>
{
    public HibernateEntityService(GraphContext context)
    {
        super(context, HibernateEntityModel.class);
    }

    /**
     * Gets an {@link Iterable} of {@link }s for the given {@link ProjectModel}.
     */
    public Iterable<HibernateEntityModel> findAllByApplication(ProjectModel application)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(application.asVertex());
        pipeline.in(HibernateEntityModel.APPLICATIONS);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, HibernateEntityModel.TYPE);

        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, HibernateEntityModel.class);
    }
}
