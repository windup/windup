package org.jboss.windup.rules.apps.javaee.service;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;

import java.util.Set;

/**
 * Contains methods for querying, updating, and deleting {@link JNDIResourceModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JNDIResourceService extends GraphService<JNDIResourceModel> {
    public JNDIResourceService(GraphContext context) {
        super(context, JNDIResourceModel.class);
    }

    /**
     * Create unique; if existing convert an existing {@link JNDIResourceModel} if one exists.
     */
    public synchronized JNDIResourceModel createUnique(Set<ProjectModel> applications, String jndiName) {
        JNDIResourceModel jndiResourceModel = getUniqueByProperty(JNDIResourceModel.JNDI_LOCATION, jndiName);
        if (jndiResourceModel == null) {
            jndiResourceModel = super.create();
            jndiResourceModel.setJndiLocation(jndiName);
            jndiResourceModel.setApplications(applications);
        } else {
            for (ProjectModel application : applications) {
                if (!jndiResourceModel.isAssociatedWithApplication(application))
                    jndiResourceModel.addApplication(application);
            }
        }
        return jndiResourceModel;
    }

    /**
     * Associate a type with the given resource model.
     */
    public void associateTypeJndiResource(JNDIResourceModel resource, String type) {
        if (type == null || resource == null) {
            return;
        }

        if (StringUtils.equals(type, "javax.sql.DataSource") && !(resource instanceof DataSourceModel)) {
            DataSourceModel ds = GraphService.addTypeToModel(this.getGraphContext(), resource, DataSourceModel.class);
        } else if (StringUtils.equals(type, "javax.jms.Queue") && !(resource instanceof JmsDestinationModel)) {
            JmsDestinationModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsDestinationModel.class);
            jms.setDestinationType(JmsDestinationType.QUEUE);
        } else if (StringUtils.equals(type, "javax.jms.QueueConnectionFactory") && !(resource instanceof JmsConnectionFactoryModel)) {
            JmsConnectionFactoryModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsConnectionFactoryModel.class);
            jms.setConnectionFactoryType(JmsDestinationType.QUEUE);
        } else if (StringUtils.equals(type, "javax.jms.Topic") && !(resource instanceof JmsDestinationModel)) {
            JmsDestinationModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsDestinationModel.class);
            jms.setDestinationType(JmsDestinationType.TOPIC);
        } else if (StringUtils.equals(type, "javax.jms.TopicConnectionFactory") && !(resource instanceof JmsConnectionFactoryModel)) {
            JmsConnectionFactoryModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsConnectionFactoryModel.class);
            jms.setConnectionFactoryType(JmsDestinationType.TOPIC);
        }
    }
}
