package org.jboss.windup.rules.apps.java.scan.dao;

import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.rules.apps.java.scan.model.JarManifestModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface JarManifestDao extends BaseDao<JarManifestModel>
{
    public boolean isManifestResource(ResourceModel resource);
    public JarManifestModel getManifestFromResource(ResourceModel resource);
}
