package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.Resource;

public interface ArchiveDao extends BaseDao<ArchiveResource> {
    public Iterable<ArchiveResource> findAllRootArchives();
    public boolean isArchiveResource(Resource resource);
    public ArchiveResource getArchiveFromResource(Resource resource);
}
