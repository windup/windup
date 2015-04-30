package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.ApplicationFlag;
import org.jboss.windup.graph.model.resource.ApplicationFlagVertex;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<FileModel>
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
    public void perform(GraphRewrite event, EvaluationContext context, FileModel resourceModel)
    {
        FileService fileModelService = new FileService(event.getGraphContext());
        resourceModel.setApplicationFlag("application");
        GraphService<ApplicationFlagVertex> service = new GraphService<ApplicationFlagVertex>(event.getGraphContext(),ApplicationFlagVertex.class);
        ApplicationFlagVertex unique = service.getUnique();
        if(unique == null) {
            unique = service.create();
        }
        recurseAndAddFiles(unique,fileModelService, resourceModel);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels
     */
    private void recurseAndAddFiles(ApplicationFlagVertex flagVertex, FileService fileService, FileModel file)
    {
        file.setApplicationFlag("application");
            file.setApplicationFlagVertex(flagVertex);
        String filePath = file.getFilePath();
        File fileReference = new File(filePath);

        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File reference : subFiles)
                {
                    FileModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    recurseAndAddFiles(flagVertex,fileService, subFile);
                }
            }
        }
    }
}
