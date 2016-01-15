package org.jboss.windup.tests.application.temporary.performance.data.initialization;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ToFileModelEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbriskar on 1/15/16.
 */
public abstract class AbstractDataInitializer
{
   abstract public void initData(GraphContext context);

    public List<FileModel> createFileModelsWithPreffix(String fileNamePreffix, int numberOfFiles, GraphContext context)
    {
        List<FileModel> createdFileModels = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++)
        {
            FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
            String fileName = fileNamePreffix + String.valueOf(i);
            fileModel.setFileName(fileName);
            createdFileModels.add(fileModel);
        }
        return createdFileModels;
    }

}
