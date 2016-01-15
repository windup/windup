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
public class TestDataInitializer extends AbstractDataInitializer
{

    private int numberOfFileModelsWithPrefix;
    private int numberOfFileModelsWithoutPrefix;

    private long totalInitDataWithoutEdge=0;
    private long totalInitDataWithEdge=0;

    public TestDataInitializer(int numberOfFileModelsWithPrefix, int numberOfFileModelsWithoutPrefix) {
        this.numberOfFileModelsWithoutPrefix=numberOfFileModelsWithoutPrefix;
        this.numberOfFileModelsWithPrefix=numberOfFileModelsWithPrefix;
    }

    @Override public void initData(GraphContext context)
    {
        Iterable<FileModel> fileModels = createTestingFileModels(context);

        ProjectModel projectModel = context.getFramed().addVertex(null, ProjectModel.class);
        //just to make sure projectModel was already loaded
        String name = projectModel.getName();

        //build data without edge property
        long initDataTimeWithoutEdgeProperty = System.nanoTime();
        linkWithoutEdgeProperty(context, projectModel, fileModels);
        context.getGraph().getBaseGraph().commit();
        initDataTimeWithoutEdgeProperty= (System.nanoTime() - initDataTimeWithoutEdgeProperty)/1000;

        //build data with edge property
        long initDataTimeWithEdgeProperty = System.nanoTime();
        linkWithEdgeProperty(context,projectModel,fileModels);
        initDataTimeWithEdgeProperty = (System.nanoTime() - initDataTimeWithEdgeProperty)/1000;
        totalInitDataWithEdge += initDataTimeWithEdgeProperty;
        totalInitDataWithoutEdge += initDataTimeWithoutEdgeProperty;
    }

    public List<FileModel> createTestingFileModels(GraphContext context)
    {
        List<FileModel> fileModelsWithPreffix = createFileModelsWithPreffix("prefix-", numberOfFileModelsWithPrefix, context);
        List<FileModel> fileModelsWithoutPreffix = createFileModelsWithPreffix("", numberOfFileModelsWithoutPrefix, context);
        List<FileModel> result = new ArrayList<>();
        result.addAll(fileModelsWithPreffix);
        result.addAll(fileModelsWithoutPreffix);
        return result;
    }

    private void linkWithoutEdgeProperty(GraphContext context, ProjectModel projectModel, Iterable<FileModel> fileModels) {
        for (FileModel fileModel : fileModels)
        {
            projectModel.addFileModel(fileModel);
        }
        context.getGraph().getBaseGraph().commit();
    }

    private void linkWithEdgeProperty(GraphContext context, ProjectModel projectModel, Iterable<FileModel> fileModels) {
        for (FileModel fileModel : fileModels)
        {
            ToFileModelEdge fileModelEdge = context.getFramed().addEdge(null, projectModel.asVertex(), fileModel.asVertex(), "toFileModelEdge",
                        ToFileModelEdge.class);
            fileModelEdge.setFileName(fileModel.getFileName());
        }
        context.getGraph().getBaseGraph().commit();
    }

    public String getTotalReport() {
        String returnString = "Total initialization of data without edge took: " + (totalInitDataWithoutEdge) + " ms. \n";
        returnString += "Total initialization of data with edge took: " + (totalInitDataWithEdge) + " ms.";
        return returnString;
    }
}
