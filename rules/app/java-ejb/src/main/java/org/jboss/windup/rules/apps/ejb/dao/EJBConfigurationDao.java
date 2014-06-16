package org.jboss.windup.rules.apps.ejb.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.ejb.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface EJBConfigurationDao extends BaseDao<EjbConfigurationFacetModel> {
    public boolean isEJBConfiguration(XmlResourceModel resource);
    public EjbConfigurationFacetModel getEjbConfigurationFromResource(XmlResourceModel resource);
}
