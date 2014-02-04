package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.ArchiveEntryResource;

public interface ArchiveEntryDao extends BaseDao<ArchiveEntryResource> {
	
	public Iterable<ArchiveEntryResource> findArchiveEntry(String value);
	public Iterable<ArchiveEntryResource> findArchiveEntryWithExtension(String ... values);
	
}
