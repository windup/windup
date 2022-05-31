package org.jboss.windup.rules.apps.javaee.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;

/**
 * Contains methods for querying, creating, and deleting {@link HibernateEntityModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class HibernateEntityService extends GraphService<HibernateEntityModel> {
    public HibernateEntityService(GraphContext context) {
        super(context, HibernateEntityModel.class);
    }

    /**
     * Gets an {@link Iterable} of {@link }s for the given {@link ProjectModel}.
     */
    public Iterable<HibernateEntityModel> findAllByApplication(ProjectModel application) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(application.getElement());
        pipeline.in(HibernateEntityModel.APPLICATIONS);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(HibernateEntityModel.TYPE));

        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline.toList(), HibernateEntityModel.class);
    }
}
