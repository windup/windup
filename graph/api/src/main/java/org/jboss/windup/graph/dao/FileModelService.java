package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.Service;

public interface FileModelService extends Service<FileModel>
{
    public FileModel createByFilePath(String filePath);

    public FileModel findByPath(String filePath);

    public Iterable<FileModel> findArchiveEntryWithExtension(String... values);
}
