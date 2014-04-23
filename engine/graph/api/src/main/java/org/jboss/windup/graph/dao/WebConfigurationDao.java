package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

public interface WebConfigurationDao extends BaseDao<WebConfigurationFacet> {
    public boolean isWebConfiguration(XmlResource resource);
    public WebConfigurationFacet getWebConfigurationFromResource(XmlResource resource);
}
