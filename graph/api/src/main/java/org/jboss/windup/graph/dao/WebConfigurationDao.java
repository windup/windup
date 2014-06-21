package org.jboss.windup.graph.dao;

import org.jboss.windup.rules.apps.web.model.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface WebConfigurationDao extends BaseDao<WebConfigurationFacetModel> {
    public boolean isWebConfiguration(XmlResourceModel resource);
    public WebConfigurationFacetModel getWebConfigurationFromResource(XmlResourceModel resource);
}
