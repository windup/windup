package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.Service;

public interface ArchiveService extends Service<ArchiveModel>
{
    public Iterable<ArchiveModel> findAllRootArchives();

    public boolean isArchiveResource(ResourceModel resource);

    public ArchiveModel getArchiveFromResource(ResourceModel resource);
}
