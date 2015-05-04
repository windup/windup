package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class DiscoverMavenHierarchyRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logging.get(DiscoverMavenProjectsRuleProvider.class);

    public DiscoverMavenHierarchyRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverMavenHierarchyRuleProvider.class)
                    .setPhase(DiscoverProjectStructurePhase.class)
                    .addExecuteAfter(DiscoverMavenProjectsRuleProvider.class)
                    .addExecuteAfter(DiscoverNonMavenArchiveProjectsRuleProvider.class)
                    .addExecuteAfter(DiscoverNonMavenSourceProjectsRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        AbstractIterationOperation<MavenProjectModel> setupParentModule = new AbstractIterationOperation<MavenProjectModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, MavenProjectModel payload)
            {
                setMavenParentProject(payload);
            }

            @Override
            public String toString()
            {
                return "ConfigureProjectHierarchy";
            }
        };

        // @formatter:off
        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(MavenProjectModel.class))
            .perform(setupParentModule);
        // @formatter:on
    }

    private void setParentProject(ArchiveModel archiveModel, MavenProjectModel projectModel)
    {
        if (archiveModel == null)
        {
            return;
        }
        else if (archiveModel.getProjectModel() != null)
        {
            String mavenGAV = projectModel.getGroupId() + ":" + projectModel.getArtifactId() + ":"
                        + projectModel.getVersion();
            String archivePath = archiveModel.getFilePath();
            LOG.info("Setting parent project for: " + mavenGAV + " to: " + archivePath);
            projectModel.setParentProject(archiveModel.getProjectModel());
        }
        else
        {
            setParentProject(archiveModel.getParentArchive(), projectModel);
        }
    }

    private void setParentProject(ResourceModel fileModel, MavenProjectModel projectModel)
    {
        if (fileModel == null)
        {
            return;
        }
        else if (fileModel.getProjectModel() != null)
        {
            projectModel.setParentProject(fileModel.getProjectModel());
        }
        else
        {
            setParentProject(fileModel.getParentArchive(), projectModel);
        }
    }

    private void setMavenParentProject(MavenProjectModel projectModel)
    {
        ResourceModel fileModel = projectModel.getRootResourceModel();
        if (fileModel == null)
        {
            // skip if no file was discovered for it
            return;
        }
        else if (fileModel instanceof ArchiveModel)
        {
            ArchiveModel archiveModel = (ArchiveModel) fileModel;
            // look at the parent archive first
            setParentProject(archiveModel.getParentArchive(), projectModel);
        }
        else
        {
            ResourceModel parentFile = fileModel.getParentFile();
            setParentProject(parentFile, projectModel);
        }
    }
}
