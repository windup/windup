package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.FileResource;

public interface FileResourceDao extends BaseDao<FileResource> {
	public FileResource createByFilePath(String filePath);
	public Iterable<FileResource> findArchiveEntryWithExtension(String ... values);
}
