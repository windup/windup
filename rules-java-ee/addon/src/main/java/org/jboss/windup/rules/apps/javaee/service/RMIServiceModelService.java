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
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.util.Logging;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Provides methods for finding, creating, and modifying {@link RMIServiceModel} instances.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class RMIServiceModelService extends GraphService<RMIServiceModel> {
    private static final Logger LOG = Logging.get(RMIServiceModelService.class);

    public RMIServiceModelService(GraphContext context) {
        super(context, RMIServiceModel.class);
    }

    public RMIServiceModel getOrCreate(ProjectModel application, JavaClassModel rmiInterface) {
        LOG.info("RMI Interface: " + rmiInterface.getQualifiedName());
        RMIServiceModel rmiServiceModel = findByInterface(rmiInterface);
        if (rmiServiceModel == null) {
            rmiServiceModel = create();
            rmiServiceModel.addApplication(application);
            rmiServiceModel.setInterface(rmiInterface);

            Iterator<JavaClassModel> implementations = rmiInterface.getImplementedBy().iterator();
            while (implementations.hasNext()) {
                JavaClassModel implModel = implementations.next();
                LOG.info(" -- Implementations: " + implModel.getQualifiedName());
                rmiServiceModel.setImplementationClass(implModel);
            }
        } else {
            if (!rmiServiceModel.isAssociatedWithApplication(application))
                rmiServiceModel.addApplication(application);
        }

        return rmiServiceModel;

    }

    private RMIServiceModel findByInterface(JavaClassModel rmiInterface) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(rmiInterface.getElement());
        pipeline.in(RMIServiceModel.RMI_INTERFACE);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.textContains(RMIServiceModel.TYPE));

        if (pipeline.hasNext()) {
            return frame(pipeline.next());
        } else {
            return null;
        }
    }
}
