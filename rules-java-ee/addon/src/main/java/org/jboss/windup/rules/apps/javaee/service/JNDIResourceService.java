package org.jboss.windup.rules.apps.javaee.service;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationType;

/**
 * Contains methods for querying, updating, and deleting {@link JNDIResourceModel}
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class JNDIResourceService extends GraphService<JNDIResourceModel>
{
    public JNDIResourceService(GraphContext context)
    {
        super(context, JNDIResourceModel.class);
    }

    /**
     * Create unique; if existing convert an existing {@link DataSourceModel} if one exists.
     */
    public synchronized JNDIResourceModel createUnique(String jndiName)
    {
        JNDIResourceModel dataSource = getUniqueByProperty(DataSourceModel.JNDI_LOCATION, jndiName);
        if (dataSource == null)
        {
            dataSource = super.create();
            dataSource.setJndiLocation(jndiName);
        }
        return dataSource;
    }

    /**
     * Associate a type with the given resource model.
     */
    public void associateTypeJndiResource(JNDIResourceModel resource, String type)
    {
        if (type == null || resource == null)
        {
            return;
        }

        if (StringUtils.equals(type, "javax.sql.DataSource") && !(resource instanceof DataSourceModel))
        {
            DataSourceModel ds = GraphService.addTypeToModel(this.getGraphContext(), resource, DataSourceModel.class);
        }
        else if (StringUtils.equals(type, "javax.jms.Queue") && !(resource instanceof JmsDestinationModel))
        {
            JmsDestinationModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsDestinationModel.class);
            jms.setDestinationType(JmsDestinationType.QUEUE);
        }
        else if (StringUtils.equals(type, "javax.jms.QueueConnectionFactory") && !(resource instanceof JmsConnectionFactoryModel))
        {
            JmsConnectionFactoryModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsConnectionFactoryModel.class);
            jms.setConnectionFactoryType(JmsDestinationType.QUEUE);
        }
        else if (StringUtils.equals(type, "javax.jms.Topic") && !(resource instanceof JmsDestinationModel))
        {
            JmsDestinationModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsDestinationModel.class);
            jms.setDestinationType(JmsDestinationType.TOPIC);
        }
        else if (StringUtils.equals(type, "javax.jms.TopicConnectionFactory") && !(resource instanceof JmsConnectionFactoryModel))
        {
            JmsConnectionFactoryModel jms = GraphService.addTypeToModel(this.getGraphContext(), resource, JmsConnectionFactoryModel.class);
            jms.setConnectionFactoryType(JmsDestinationType.TOPIC);
        }
    }
}
