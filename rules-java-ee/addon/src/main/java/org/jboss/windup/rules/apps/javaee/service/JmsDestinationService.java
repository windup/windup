package org.jboss.windup.rules.apps.javaee.service;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;

import java.util.Set;

/**
 * Contains methods for querying, updating, and deleting {@link JmsDestinationModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class JmsDestinationService extends GraphService<JmsDestinationModel> {
    private final JNDIResourceService jndiResourceService;

    /**
     * Creates a new {@link JmsDestinationService} instance.
     */
    public JmsDestinationService(GraphContext context) {
        super(context, JmsDestinationModel.class);
        this.jndiResourceService = new JNDIResourceService(context);
    }

    /**
     * Gets JmsDestinationType from java class name
     * <p>
     * Returns null for unrecognized class
     */
    public static JmsDestinationType getTypeFromClass(String aClass) {
        if (StringUtils.equals(aClass, "javax.jms.Queue") || StringUtils.equals(aClass, "javax.jms.QueueConnectionFactory")) {
            return JmsDestinationType.QUEUE;
        } else if (StringUtils.equals(aClass, "javax.jms.Topic") || StringUtils.equals(aClass, "javax.jms.TopicConnectionFactory")) {
            return JmsDestinationType.TOPIC;
        } else {
            return null;
        }
    }

    /**
     * Creates a new instance with the given name, or converts an existing instance at this location if one already exists
     */
    public JmsDestinationModel createUnique(Set<ProjectModel> applications, String jndiName, String destinationTypeClass) {
        JmsDestinationType destinationType = JmsDestinationService.getTypeFromClass(destinationTypeClass);

        return this.createUnique(applications, jndiName, destinationType);
    }

    /**
     * Creates a new instance with the given name, or converts an existing instance at this location if one already exists
     */
    public JmsDestinationModel createUnique(Set<ProjectModel> applications, String jndiName, JmsDestinationType destinationType) {
        JmsDestinationModel model = createUnique(applications, jndiName);
        model.setDestinationType(destinationType);

        return model;
    }

    public JmsDestinationModel createUnique(Set<ProjectModel> applications, String jndiName) {
        JmsDestinationModel model = null;

        JNDIResourceModel jndiRef = jndiResourceService.createUnique(applications, jndiName);
        if (jndiRef instanceof JmsDestinationModel) {
            model = (JmsDestinationModel) jndiRef;
        } else {
            model = this.addTypeToModel(jndiRef);
        }

        return model;
    }
}
