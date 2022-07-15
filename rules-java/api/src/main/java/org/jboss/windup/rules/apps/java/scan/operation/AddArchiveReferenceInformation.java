package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.ApplicationArchiveModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddArchiveReferenceInformation extends AbstractIterationOperation<FileModel> {
    private AddArchiveReferenceInformation(String variableName) {
        super(variableName);
    }

    public AddArchiveReferenceInformation() {
        super();
    }

    public static AddArchiveReferenceInformation to(String variableName) {
        return new AddArchiveReferenceInformation(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel fileModel) {
        File file = new File(fileModel.getFilePath());
        ArchiveModel archiveModel = GraphService.addTypeToModel(event.getGraphContext(), fileModel, ArchiveModel.class);

        archiveModel.setArchiveName(file.getName());

        GraphService.addTypeToModel(event.getGraphContext(), fileModel, ApplicationArchiveModel.class);

        // This line will cause the file to be marked if it is to be ignored
        new WindupJavaConfigurationService(event.getGraphContext()).checkRegexAndIgnore(event, fileModel);
    }

    @Override
    public String toString() {
        return "AddArchiveReferenceInformation";
    }
}
