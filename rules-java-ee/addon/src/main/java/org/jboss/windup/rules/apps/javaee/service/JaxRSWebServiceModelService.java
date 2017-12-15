package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Provides methods for creating, updating, and deleting {@link JaxRSWebServiceModelService} vertices.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JaxRSWebServiceModelService extends GraphService<JaxRSWebServiceModel>
{
    public JaxRSWebServiceModelService(GraphContext context)
    {
        super(context, JaxRSWebServiceModel.class);
    }

    public JaxRSWebServiceModel getOrCreate(ProjectModel application, String path, JavaClassModel implementationClass)
    {
        GraphTraversal<Vertex, Vertex> pipeline;
        if (implementationClass == null)
        {
            pipeline = new GraphTraversal<>(getGraphContext().getGraph());
            pipeline.V();
            pipeline.has(WindupVertexFrame.TYPE_PROP, JaxRSWebServiceModel.TYPE);
        }
        else
        {
            pipeline = new GraphTraversal<>(implementationClass.asVertex());
            pipeline.out(JaxRSWebServiceModel.JAXRS_IMPLEMENTATION_CLASS);
            pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, JaxRSWebServiceModel.TYPE);
        }
        pipeline.has(JaxRSWebServiceModel.PATH, path);

        if (pipeline.hasNext())
        {
            JaxRSWebServiceModel result = frame(pipeline.next());
            if (!result.isAssociatedWithApplication(application))
                result.addApplication(application);
            return result;
        }
        else
        {
            JaxRSWebServiceModel jaxWebService = create();
            jaxWebService.addApplication(application);
            jaxWebService.setPath(path);

            jaxWebService.setImplementationClass(implementationClass);
            return jaxWebService;
        }
    }
}
