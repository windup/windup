package org.jboss.windup.rules.apps.java.scan.operation;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;

/**
 * Recurses the given folder and creates the FileModels vertices for the child files to the graph.
 */
public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<FileModel> {

    /**
     * Let the variable name to be set by the current Iteration.
     */
    public RecurseDirectoryAndAddFiles() {
        super();
    }

    @Override
    public String toString() {
        return "RecurseDirectoryAndAddFiles";
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel resourceModel) {
        FileService fileModelService = new FileService(event.getGraphContext());
        WindupJavaConfigurationService javaConfigurationService = new WindupJavaConfigurationService(event.getGraphContext());
        recurseAndAddFiles(event, fileModelService, javaConfigurationService, resourceModel);
    }

    /**
     * Recurses the given folder and creates the FileModels vertices for the child files to the graph.
     */
    private void recurseAndAddFiles(GraphRewrite event, FileService fileService, WindupJavaConfigurationService javaConfigurationService, FileModel file) {
        if (javaConfigurationService.checkRegexAndIgnore(event, file))
            return;

        String filePath = file.getFilePath();
        File fileReference = new File(filePath);
        long directorySize = 0L;

        if (fileReference.isDirectory()) {
            File[] subFiles = fileReference.listFiles();
            if (subFiles != null) {
                for (File reference : subFiles) {
                    // Check if the current dir is a maven target folder and ignore it if so (WINDUP-3234)
                    if (javaConfigurationService.isTargetDir(file)) continue;

                    FileModel subFile = fileService.createByFilePath(file, reference.getAbsolutePath());
                    recurseAndAddFiles(event, fileService, javaConfigurationService, subFile);
                    if (subFile.isDirectory()) {
                        directorySize = directorySize + subFile.getDirectorySize();
                    } else {
                        directorySize = directorySize + subFile.getSize();
                    }
                }
            }
            file.setDirectorySize(directorySize);
        }
    }
}
