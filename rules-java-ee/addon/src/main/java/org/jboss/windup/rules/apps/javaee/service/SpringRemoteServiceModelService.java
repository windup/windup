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
import org.jboss.windup.rules.apps.javaee.model.SpringRemoteServiceModel;
import org.jboss.windup.util.Logging;

import java.util.logging.Logger;

/**
 * Provides methods for finding, creating, and modifying {@link SpringRemoteServiceModel} instances.
 *
 */
public class SpringRemoteServiceModelService extends GraphService<SpringRemoteServiceModel> {
    private static final Logger LOG = Logging.get(SpringRemoteServiceModelService.class);

    public SpringRemoteServiceModelService(GraphContext context)
    {
        super(context, SpringRemoteServiceModel.class);
    }

    public SpringRemoteServiceModel getOrCreate(ProjectModel application, JavaClassModel remoteInterface, JavaClassModel exporterInterface) {
        SpringRemoteServiceModel remoteServiceModel = findByInterfaceAndExporter(remoteInterface, exporterInterface);
        if (remoteServiceModel == null) {
            remoteServiceModel = create();
            remoteServiceModel.addApplication(application);
            remoteServiceModel.setInterface(remoteInterface);
            remoteServiceModel.setSpringExporterInterface(exporterInterface);

            remoteServiceModel.setImplementationClass(remoteInterface.getImplementedBy().stream().findFirst().orElse(null));
        }
        else {
            if (!remoteServiceModel.isAssociatedWithApplication(application))
                remoteServiceModel.addApplication(application);
        }

        return remoteServiceModel;
    }

    private SpringRemoteServiceModel findByInterfaceAndExporter(JavaClassModel rmiInterface, JavaClassModel exporterInterface) {
        return this.findAll().stream().filter(e-> e.getInterface().getQualifiedName().equalsIgnoreCase(rmiInterface.getQualifiedName()) &&
                 e.getSpringExporterInterface().getQualifiedName().equalsIgnoreCase(exporterInterface.getQualifiedName())).findFirst().orElse(null);
    }

    public String getTagName(String exporterClass) {
        if (exporterClass.contains("RmiServiceExporter")) {
            return "spring-rmi";
        } else if (exporterClass.contains("HttpInvokerServiceExporter")) {
            return "spring-httpinvoker";
        } else if (exporterClass.contains("HessianServiceExporter")) {
            return "spring-hessian";
        } else if (exporterClass.contains("SimpleJaxWsServiceExporter")) {
            return "spring-jaxws";
        } else if (exporterClass.contains("JmsInvokerServiceExporter")) {
            return "spring-jms";
        } else if (exporterClass.contains("AmqpInvokerServiceExporter")) {
            return "spring-amqp";
        } else return "spring-undefined";
    }
}
