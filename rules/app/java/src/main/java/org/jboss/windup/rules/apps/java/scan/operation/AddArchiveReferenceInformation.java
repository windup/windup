package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.ApplicationArchiveModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.util.GraphUtil;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddArchiveReferenceInformation extends AbstractIterationOperation<FileModel>
{
    private AddArchiveReferenceInformation(String variableName)
    {
        super(FileModel.class, variableName);
    }

    public static AddArchiveReferenceInformation to(String variableName)
    {
        return new AddArchiveReferenceInformation(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel fileResourceModel)
    {
        File file = new File(fileResourceModel.getFilePath());
        ArchiveModel archiveResourceModel = GraphUtil.addTypeToModel(event.getGraphContext(),
                    fileResourceModel, ArchiveModel.class);

        archiveResourceModel.setArchiveName(file.getName());

        ApplicationArchiveModel appArchiveModel = event.getGraphContext().getFramed()
                    .addVertex(null, ApplicationArchiveModel.class);
        appArchiveModel.setOriginalArchive(archiveResourceModel);
        appArchiveModel.setApplicationName(file.getName());
    }
}
