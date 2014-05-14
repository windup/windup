package org.jboss.windup.engine.provider;

import javax.inject.Inject;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.engine.util.ZipUtil;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class FileScannerWindupConfigurationProvider extends WindupConfigurationProvider
{
    @Inject
    ApplicationReferenceDao applicationReferenceDao;

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                                GraphSearchConditionBuilder
                                            .create("inputFiles")
                                            .ofType(FileResourceModel.class)

                    )
                    .perform(
                                Iteration.over("inputFiles").var(FileResourceModel.class, "file")
                                            .perform(new GraphOperation()
                                            {

                                                @Override
                                                public void perform(GraphRewrite event, EvaluationContext context)
                                                {
                                                    GraphContext graphContext = event.getGraphContext();
                                                    SelectionFactory factory = SelectionFactory.instance(event);
                                                    FileResourceModel fileModel = factory.getCurrentPayload(
                                                                FileResourceModel.class, "file");
                                                    if (ZipUtil.endsWithZipExtension(fileModel.getFilePath()))
                                                    {
                                                        java.io.File file = new java.io.File(fileModel.getFilePath());
                                                        ArchiveResourceModel archiveResourceModel = GraphUtil
                                                                    .addTypeToModel(graphContext, fileModel,
                                                                                ArchiveResourceModel.class);
                                                        archiveResourceModel.setArchiveName(file.getName());

                                                        ApplicationReferenceModel appRefModel = applicationReferenceDao
                                                                    .create();
                                                        appRefModel.setArchive(archiveResourceModel);
                                                    }
                                                }
                                            })
                    );
    }
}
