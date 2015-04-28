package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.FileService;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<ResourceModel>
{
    private RecurseDirectoryAndAddFiles(String variableName)
    {
        super(variableName);
    }

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public RecurseDirectoryAndAddFiles()
    {
        super();
    }

    public static RecurseDirectoryAndAddFiles startingAt(String variableName)
    {
        return new RecurseDirectoryAndAddFiles(variableName);
    }

    @Override
    public String toString()
    {
        return "RecurseDirectoryAndAddFiles";
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ResourceModel resourceModel)
    {
        FileService fileModelService = new FileService(event.getGraphContext());
        recurseAndAddFiles(fileModelService, resourceModel);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as ResourceModels
     */
    private void recurseAndAddFiles(FileService fileService, ResourceModel file)
    {
        String filePath = file.getFilePath();
        File fileReference = new File(filePath);

        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File reference : subFiles)
                {
                    ResourceModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    recurseAndAddFiles(fileService, subFile);
                }
            }
        }
    }
}
