package org.jboss.windup.addon.config.operation.ruleelement;

import java.io.File;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddArchiveReferenceInformation extends GraphOperation
{

    public static AddArchiveReferenceInformation addReferenceInformation()
    {
        return new AddArchiveReferenceInformation();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        SelectionFactory factory = SelectionFactory
                    .instance(event);

        FileResourceModel fileResourceModel = factory
                    .getCurrentPayload(
                                FileResourceModel.class, "file");
        File file = new File(fileResourceModel.getFilePath());
        ArchiveResourceModel archiveResourceModel = factory
                    .getCurrentPayload(
                                ArchiveResourceModel.class,
                                "file");

        archiveResourceModel.setArchiveName(file.getName());

        ApplicationReferenceModel appRefModel = event.getGraphContext().getFramed()
                    .addVertex(null, ApplicationReferenceModel.class);
        appRefModel.setArchive(archiveResourceModel);
    }
}
