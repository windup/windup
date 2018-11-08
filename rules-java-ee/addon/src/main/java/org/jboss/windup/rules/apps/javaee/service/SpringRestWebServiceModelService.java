package org.jboss.windup.rules.apps.javaee.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.SpringRestWebServiceModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Provides methods for creating, updating, and deleting {@link SpringRestWebServiceModelService} vertices.
 *
 */
public class SpringRestWebServiceModelService extends GraphService<SpringRestWebServiceModel>
{
    public SpringRestWebServiceModelService(GraphContext context)
    {
        super(context, SpringRestWebServiceModel.class);
    }

    public SpringRestWebServiceModel getOrCreate(ProjectModel application, String path, JavaClassModel implementationClass)
    {
        GraphTraversal<Vertex, Vertex> pipeline;
        if (implementationClass == null)
        {
            pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V();
            pipeline.has(WindupVertexFrame.TYPE_PROP, SpringRestWebServiceModel.TYPE);
        }
        else
        {
            pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(implementationClass.getElement());
            pipeline.out(SpringRestWebServiceModel.JAXRS_IMPLEMENTATION_CLASS);
            pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(SpringRestWebServiceModel.TYPE));
        }
        pipeline.has(SpringRestWebServiceModel.PATH, path);

        if (pipeline.hasNext())
        {
            SpringRestWebServiceModel result = frame(pipeline.next());
            if (!result.isAssociatedWithApplication(application))
                result.addApplication(application);
            return result;
        }
        else
        {
            SpringRestWebServiceModel jaxWebService = create();
            jaxWebService.addApplication(application);
            jaxWebService.setPath(path);

            jaxWebService.setImplementationClass(implementationClass);
            return jaxWebService;
        }
    }
}
