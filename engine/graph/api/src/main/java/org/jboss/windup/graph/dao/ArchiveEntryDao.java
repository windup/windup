package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;

public interface ArchiveEntryDao extends BaseDao<ArchiveEntryResourceModel>
{

    public Iterable<ArchiveEntryResourceModel> findArchiveEntry(String value);

    public long findArchiveEntryWithExtensionCount(String... values);

    public Iterable<ArchiveEntryResourceModel> findArchiveEntryWithExtension(String... values);

    public Iterable<ArchiveEntryResourceModel> findByArchive(final ArchiveResourceModel resource);
}
