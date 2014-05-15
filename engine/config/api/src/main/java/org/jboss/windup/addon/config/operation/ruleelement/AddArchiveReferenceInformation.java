package org.jboss.windup.addon.config.operation.ruleelement;

import java.io.File;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddArchiveReferenceInformation extends AbstractIterationRuleElement<FileResourceModel>
{
    public AddArchiveReferenceInformation(String variableName)
    {
        super(FileResourceModel.class, variableName);
    }

    public static AddArchiveReferenceInformation addReferenceInformation(String variableName)
    {
        return new AddArchiveReferenceInformation(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileResourceModel fileResourceModel)
    {
        File file = new File(fileResourceModel.getFilePath());
        ArchiveResourceModel archiveResourceModel = GraphUtil.addTypeToModel(event.getGraphContext(),
                    fileResourceModel, ArchiveResourceModel.class);

        archiveResourceModel.setArchiveName(file.getName());

        ApplicationReferenceModel appRefModel = event.getGraphContext().getFramed()
                    .addVertex(null, ApplicationReferenceModel.class);
        appRefModel.setArchive(archiveResourceModel);
    }
}
