package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.FileModel;

public interface FileResourceDao extends BaseDao<FileModel> {
	public FileModel createByFilePath(String filePath);
	public Iterable<FileModel> findArchiveEntryWithExtension(String ... values);
}
