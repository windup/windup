package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.resource.FileModel;

public class FileLocationService extends GraphService<FileLocationModel>
{
    private static final String VERTEX_LABEL = "fileLocations";

    public FileLocationService(GraphContext context)
    {
        super(context, FileLocationModel.class);
    }

    /**
     * Gets a {@link FileLocationModel} with the given parameters.
     */
    public FileLocationModel getUnique(FileModel fileModel, int columnNumber, int lineNumber, int length, String sourceSnippit)
    {
        return getUnique(getQuery().getRawTraversal()
                .has(FileLocationModel.COLUMN_NUMBER, columnNumber)
                .has(FileLocationModel.LINE_NUMBER,lineNumber)
                .has(FileLocationModel.LENGTH,length)
                .has(FileLocationModel.SOURCE_SNIPPIT,sourceSnippit)
                .as(VERTEX_LABEL)
                .out(FileReferenceModel.FILE_MODEL)
                .has(FileModel.FILE_PATH, fileModel.getFilePath())
                .select(VERTEX_LABEL));
    }

    /**
     * This essentially ensures that we only store a single Vertex for each unique and different FileLocationModel
     */
    public FileLocationModel getOrCreate(FileModel fileModel, int columnNumber, int lineNumber, int length, String sourceSnippit)
    {
        FileLocationModel fileLocationModel = getUnique(fileModel, columnNumber, lineNumber, length, sourceSnippit);
        if (fileLocationModel == null)
        {
            fileLocationModel = create();
            fileLocationModel.setFile(fileModel);
            fileLocationModel.setColumnNumber(columnNumber);
            fileLocationModel.setLineNumber(lineNumber);
            fileLocationModel.setLength(length);
            fileLocationModel.setSourceSnippit(sourceSnippit);
        }
        return fileLocationModel;
    }

}
