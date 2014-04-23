package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;

public interface EJBConfigurationDao extends BaseDao<EjbConfigurationFacet> {
    public boolean isEJBConfiguration(XmlResource resource);
    public EjbConfigurationFacet getEjbConfigurationFromResource(XmlResource resource);
}
