package org.jboss.windup.rules.apps.javaee.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.SpringRemoteServiceModel;
import org.jboss.windup.util.Logging;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Provides methods for finding, creating, and modifying {@link SpringRemoteServiceModel} instances.
 *
 */
public class SpringRemoteServiceModelService extends GraphService<SpringRemoteServiceModel>
{
    private static final Logger LOG = Logging.get(SpringRemoteServiceModelService.class);

    public SpringRemoteServiceModelService(GraphContext context)
    {
        super(context, SpringRemoteServiceModel.class);
    }

    public SpringRemoteServiceModel getOrCreate(ProjectModel application, JavaClassModel remoteInterface, JavaClassModel exporterInterface)
    {
        LOG.info("Spring Remote Interface: " + remoteInterface.getQualifiedName());
        SpringRemoteServiceModel remoteServiceModel = findByInterface(remoteInterface);
        if (remoteServiceModel == null)
        {
            remoteServiceModel = create();
            remoteServiceModel.addApplication(application);
            remoteServiceModel.setInterface(remoteInterface);
            remoteServiceModel.setSpringExporterInterface(exporterInterface);

            Iterator<JavaClassModel> implementations = remoteInterface.getImplementedBy().iterator();
            while (implementations.hasNext())
            {
                JavaClassModel implModel = implementations.next();
                LOG.info(" -- Implementations: " + implModel.getQualifiedName());
                remoteServiceModel.setImplementationClass(implModel);
            }
        }
        else
        {
            if (!remoteServiceModel.isAssociatedWithApplication(application))
                remoteServiceModel.addApplication(application);
        }

        return remoteServiceModel;

    }

    private SpringRemoteServiceModel findByInterface(JavaClassModel rmiInterface)
    {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(rmiInterface.getElement());
        pipeline.in(SpringRemoteServiceModel.REMOTESERVICE_INTERFACE);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(SpringRemoteServiceModel.TYPE));

        if (pipeline.hasNext())
        {
            return frame(pipeline.next());
        }
        else
        {
            return null;
        }
    }
}
