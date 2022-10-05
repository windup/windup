package org.jboss.windup.rules.apps.javaee.service;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;

/**
 * Contains methods for creating, querying, and updating SpringBeanModel entries in the Graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SpringBeanService extends GraphService<SpringBeanModel> {
    public SpringBeanService(GraphContext context) {
        super(context, SpringBeanModel.class);
    }

    public Iterable<SpringBeanModel> findAllBySpringBeanName(String name) {
        return super.findAllByProperty(SpringBeanModel.SPRING_BEAN_NAME, name);
    }

    /**
     * Gets an {@link Iterable} of {@link SpringBeanModel}s for the given {@link ProjectModel}.
     *
     * @return an iterable of SpringBeanModel entries for the given application
     */
    public Iterable<SpringBeanModel> findAllByApplication(ProjectModel application) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(application.getElement());
        pipeline.in(SpringBeanModel.APPLICATIONS);
        pipeline.has(WindupVertexFrame.TYPE_PROP, P.eq(SpringBeanModel.TYPE));

        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline.toList(), SpringBeanModel.class);
    }
}
