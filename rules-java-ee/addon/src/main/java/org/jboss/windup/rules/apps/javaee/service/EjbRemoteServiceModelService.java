package org.jboss.windup.rules.apps.javaee.service;

import com.google.common.collect.Iterables;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;

/**
 * Contains methods for managing {@link EjbRemoteServiceModel} instances.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class EjbRemoteServiceModelService extends GraphService<EjbRemoteServiceModel> {
    public EjbRemoteServiceModelService(GraphContext context) {
        super(context, EjbRemoteServiceModel.class);
    }

    /**
     * Either creates a new {@link EjbRemoteServiceModel} or returns an existing one if one already exists.
     */
    public EjbRemoteServiceModel getOrCreate(Iterable<ProjectModel> applications, JavaClassModel remoteInterface, JavaClassModel implementationClass) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V();
        pipeline.has(WindupVertexFrame.TYPE_PROP, EjbRemoteServiceModel.TYPE);
        if (remoteInterface != null)
            pipeline.as("remoteInterface").out(EjbRemoteServiceModel.EJB_INTERFACE)
                    .filter(vertexTraverser -> vertexTraverser.get().equals(remoteInterface.getElement()))
                    .select("remoteInterface");

        if (implementationClass != null)
            pipeline.as("implementationClass").out(EjbRemoteServiceModel.EJB_IMPLEMENTATION_CLASS)
                    .filter(vertexTraverser -> vertexTraverser.get().equals(implementationClass.getElement()))
                    .select("implementationClass");

        if (pipeline.hasNext()) {
            EjbRemoteServiceModel result = frame(pipeline.next());
            for (ProjectModel application : applications) {
                if (!Iterables.contains(result.getApplications(), application))
                    result.addApplication(application);
            }
            return result;
        } else {
            EjbRemoteServiceModel model = create();
            model.setApplications(applications);
            model.setInterface(remoteInterface);
            model.setImplementationClass(implementationClass);
            return model;
        }
    }
}
