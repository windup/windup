package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.context.EvaluationContext;

import static java.lang.String.format;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
     * Recurses the given folder and creates the FileModels vertices for the child files to the graph.
     */
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
        WindupJavaConfigurationService javaConfigurationService = new WindupJavaConfigurationService(event.getGraphContext());
        recurseAndAddFiles(event, fileModelService, javaConfigurationService, resourceModel);
    }

    /**
     * Recurses the given folder and creates the FileModels vertices for the child files to the graph.
     */
    private void recurseAndAddFiles(GraphRewrite event, FileService fileService, WindupJavaConfigurationService javaConfigurationService, FileModel file)
    {
        if (javaConfigurationService.checkIfIgnored(event, file))
            return;

        String filePath = file.getFilePath();
        File fileReference = new File(filePath);
        long directorySize = 0L;

        if (fileReference.isDirectory())
        {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null)
            {
                for (File reference : subFiles)
                {
                    // Check if the current dir is a maven target folder and ignore it if so (WINDUP-3234)
                    if (javaConfigurationService.isTargetDir(file)) continue;
                    
                    FileModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    recurseAndAddFiles(event, fileService, javaConfigurationService, subFile);
                    if (subFile.isDirectory())
                    {
                        directorySize = directorySize + subFile.getDirectorySize();
                    }
                    else
                    {
                        directorySize = directorySize + subFile.getSize();
                    }
                }
            }
            file.setDirectorySize(directorySize);
        }
    }
}
