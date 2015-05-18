package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;

/**
 * Contains methods for querying, updating, and deleting {@link DataSourceModel}
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class DataSourceService extends GraphService<DataSourceModel>
{
    public DataSourceService(GraphContext context)
    {
        super(context, DataSourceModel.class);
    }

    /**
     * Create unique; if existing convert an existing {@link DataSourceModel} if one exists.
     */
    public synchronized DataSourceModel createUnique(String dataSourceName, String jndiName)
    {
        DataSourceModel dataSource = getUniqueByProperty(DataSourceModel.JNDI_LOCATION, jndiName);
        if (dataSource == null)
        {
            dataSource = super.create();
            dataSource.setName(dataSourceName);
            dataSource.setJndiLocation(jndiName);
        }
        return dataSource;
    }
}
