package org.jboss.windup.rules.apps.javaee.service;

import java.util.Collections;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JaxWSWebServiceModelService extends GraphService<JaxWSWebServiceModel>
{
    public JaxWSWebServiceModelService(GraphContext context)
    {
        super(context, JaxWSWebServiceModel.class);
    }

    public JaxWSWebServiceModel getOrCreate(ProjectModel application, JavaClassModel endpointInterface, JavaClassModel implementationClass)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(getGraphContext().getGraph());
        pipeline.V().has(WindupVertexFrame.TYPE_PROP, JaxWSWebServiceModel.TYPE);
        if (endpointInterface != null)
            pipeline.as("endpointInterface").out(JaxWSWebServiceModel.JAXWS_INTERFACE).retain(Collections.singleton(endpointInterface.asVertex()))
                        .back("endpointInterface");

        if (implementationClass != null)
            pipeline.as("implementationClass").out(JaxWSWebServiceModel.JAXWS_IMPLEMENTATION_CLASS)
                        .retain(Collections.singleton(implementationClass.asVertex()))
                        .back("implementationClass");

        if (pipeline.hasNext())
        {
            JaxWSWebServiceModel result = frame(pipeline.next());
            if (!result.isAssociatedWithApplication(application))
                result.addApplication(application);
            return result;
        }
        else
        {
            JaxWSWebServiceModel model = create();
            model.addApplication(application);
            model.setInterface(endpointInterface);
            if (implementationClass != null)
            {
                model.setImplementationClass(implementationClass);
            }
            return model;
        }
    }
}
