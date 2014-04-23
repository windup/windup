package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.PropertiesMeta;
import org.jboss.windup.graph.model.resource.Resource;

public interface PropertiesDao extends BaseDao<PropertiesMeta>
{
    public boolean isPropertiesResource(Resource resource);
    public PropertiesMeta getPropertiesFromResource(Resource resource);
}
