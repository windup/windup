package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.FileModelService;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.maven.dao.MavenModelService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoverMavenHierarchyRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(DiscoverMavenProjectsRuleProvider.class);

    @Inject
    private MavenModelService mavenModelService;
    @Inject
    private FileModelService fileModelService;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(DiscoverMavenProjectsRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        ConditionBuilder allProjects = Query
            .find(MavenProjectModel.class);

        AbstractIterationOperation<MavenProjectModel> setupParentModule = new AbstractIterationOperation<MavenProjectModel>(
                    MavenProjectModel.class)
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, MavenProjectModel payload)
            {
                setMavenParentProject(payload);
            }
        };

        return ConfigurationBuilder.begin()
            .addRule()
            .when(allProjects)
            .perform(
                Iteration.over().perform(setupParentModule)
                        .endIteration()
            );
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

    private void setParentProject(FileModel fileModel, MavenProjectModel projectModel)
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
        FileModel fileModel = projectModel.getRootFileModel();
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
            FileModel parentFile = fileModel.getParentFile();
            setParentProject(parentFile, projectModel);
        }
    }
}
