package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface JarManifestDao extends BaseDao<JarManifestModel>
{
    public boolean isManifestResource(ResourceModel resource);
    public JarManifestModel getManifestFromResource(ResourceModel resource);
}
