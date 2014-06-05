package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.FileResourceModel;

public interface FileResourceDao extends BaseDao<FileResourceModel> {
	public FileResourceModel createByFilePath(String filePath);
	public Iterable<FileResourceModel> findArchiveEntryWithExtension(String ... values);
}
