package org.jboss.windup.rules.apps.javaee.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;

import java.util.Set;

/**
 * Contains methods for querying, updating, and deleting {@link DataSourceModel}
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DataSourceService extends GraphService<DataSourceModel> {
    public DataSourceService(GraphContext context) {
        super(context, DataSourceModel.class);
    }

    /**
     * Create unique; if existing convert an existing {@link DataSourceModel} if one exists.
     */
    public synchronized DataSourceModel createUnique(Set<ProjectModel> applications, String dataSourceName, String jndiName) {
        JNDIResourceModel jndiResourceModel = new JNDIResourceService(getGraphContext()).createUnique(applications, jndiName);
        final DataSourceModel dataSourceModel;
        if (jndiResourceModel instanceof DataSourceModel) {
            dataSourceModel = (DataSourceModel) jndiResourceModel;
        } else {
            dataSourceModel = addTypeToModel(jndiResourceModel);
        }
        dataSourceModel.setName(dataSourceName);
        return dataSourceModel;
    }
}
