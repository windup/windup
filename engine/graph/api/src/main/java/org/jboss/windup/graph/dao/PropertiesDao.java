package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface PropertiesDao extends BaseDao<PropertiesMetaModel>
{
    public boolean isPropertiesResource(ResourceModel resource);
    public PropertiesMetaModel getPropertiesFromResource(ResourceModel resource);
}
