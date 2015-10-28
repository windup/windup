package org.jboss.windup.rules.apps.javaee.service;

import java.util.Collections;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Contains methods for managing {@link EjbRemoteServiceModel} instances.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EjbRemoteServiceModelService extends GraphService<EjbRemoteServiceModel>
{
    public EjbRemoteServiceModelService(GraphContext context)
    {
        super(context, EjbRemoteServiceModel.class);
    }

    /**
     * Either creates a new {@link EjbRemoteServiceModel} or returns an existing one if one already exists.
     */
    public EjbRemoteServiceModel getOrCreate(ProjectModel application, JavaClassModel remoteInterface, JavaClassModel implementationClass)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(getGraphContext().getGraph());
        pipeline.V().has(WindupVertexFrame.TYPE_PROP, EjbRemoteServiceModel.TYPE);
        if (remoteInterface != null)
            pipeline.as("remoteInterface").out(EjbRemoteServiceModel.EJB_INTERFACE).retain(Collections.singleton(remoteInterface.asVertex()))
                        .back("remoteInterface");

        if (implementationClass != null)
            pipeline.as("implementationClass").out(EjbRemoteServiceModel.EJB_IMPLEMENTATION_CLASS)
                        .retain(Collections.singleton(implementationClass.asVertex()))
                        .back("implementationClass");

        if (pipeline.hasNext())
        {
            EjbRemoteServiceModel result = frame(pipeline.next());
            if (!result.isAssociatedWithApplication(application))
            {
                result.addApplication(application);
            }
            return result;
        }
        else
        {
            EjbRemoteServiceModel model = create();
            model.addApplication(application);
            model.setInterface(remoteInterface);
            model.setImplementationClass(implementationClass);
            return model;
        }
    }
}
