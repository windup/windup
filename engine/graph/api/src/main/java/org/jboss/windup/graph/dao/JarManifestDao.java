package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.resource.Resource;

public interface JarManifestDao extends BaseDao<JarManifest>
{
    public boolean isManifestResource(Resource resource);
    public JarManifest getManifestFromResource(Resource resource);
}
