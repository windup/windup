package org.jboss.windup.rules.apps.javaee.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.forge.roaster._shade.org.eclipse.core.internal.resources.Project;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;

/**
 * Contains methods for creating, querying, and updating SpringBeanModel entries in the Graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class SpringBeanService extends GraphService<SpringBeanModel>
{
    public SpringBeanService(GraphContext context)
    {
        super(context, SpringBeanModel.class);
    }

    public Iterable<SpringBeanModel> findAllBySpringBeanName(String name)
    {
        return super.findAllByProperty(SpringBeanModel.SPRING_BEAN_NAME, name);
    }

    /**
     * Gets an {@link Iterable} of {@link SpringBeanModel}s for the given {@link ProjectModel}.
     *
     * @return an iterable of SpringBeanModel entries for the given application
     */
    public Iterable<SpringBeanModel> findAllByApplication(ProjectModel application)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(application.asVertex());
        pipeline.in(SpringBeanModel.APPLICATIONS);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, SpringBeanModel.TYPE);

        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, SpringBeanModel.class);
    }
}
