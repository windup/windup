package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

public interface ArchiveDao extends BaseDao<ArchiveResourceModel> {
    public Iterable<ArchiveResourceModel> findAllRootArchives();
    public boolean isArchiveResource(ResourceModel resource);
    public ArchiveResourceModel getArchiveFromResource(ResourceModel resource);
}
