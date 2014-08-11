package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.dao.FileModelService;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<FileModel>
{
    private RecurseDirectoryAndAddFiles(String variableName)
    {
        super(FileModel.class, variableName);
    }
    
    public RecurseDirectoryAndAddFiles()
    {
        super(FileModel.class);
    }

    public static RecurseDirectoryAndAddFiles startingAt(String variableName)
    {
        return new RecurseDirectoryAndAddFiles(variableName);
    }
    

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel resourceModel)
    {
        FileModelService fileModelService = new FileModelService(event.getGraphContext());
        recurseAndAddFiles(fileModelService, resourceModel);
    }

    /**
     * Recurses the given folder and adds references to these files to the graph as FileModels
     */
    private void recurseAndAddFiles(FileModelService fileService, FileModel file)
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
                    FileModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    recurseAndAddFiles(fileService, subFile);
                }
            }
        }
    }
}
