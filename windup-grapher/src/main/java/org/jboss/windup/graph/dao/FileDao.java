package org.jboss.windup.graph.dao;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.windup.graph.model.resource.File;

public interface FileDao extends BaseDao<File> {
	public File getByFilePath(String filePath);
	
	public InputStream getPayload(File file) throws IOException;
	public Iterable<File> findArchiveEntryWithExtension(String ... values);
}
