package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds files that were not classified as Maven archives/projects, and adds some generic project information for them.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverNonMavenSourceProjectsRuleProvider extends AbstractRuleProvider
{
    public DiscoverNonMavenSourceProjectsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverNonMavenSourceProjectsRuleProvider.class)
                    .setPhase(DiscoverProjectStructurePhase.class)
                    .addExecuteAfter(DiscoverNonMavenArchiveProjectsRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(new AddProjectInformation());
    }

    private class AddProjectInformation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            ResourceModel mainResourceModel = cfg.getInputPath();

            ProjectService projectModelService = new ProjectService(event.getGraphContext());
            ProjectModel mainProjectModel = mainResourceModel.getProjectModel();
            if (mainProjectModel == null)
            {
                mainProjectModel = projectModelService.create();
                mainProjectModel.setName(mainResourceModel.getFileName());
                mainProjectModel.setDescription("Source Directory");

                mainResourceModel.setProjectModel(mainProjectModel);
                mainProjectModel.setRootResourceModel(mainResourceModel);
                mainProjectModel.addResourceModel(mainResourceModel);
            }

            addProjectToChildFiles(mainResourceModel, mainProjectModel);
        }

        private void addProjectToChildFiles(ResourceModel fileModel, ProjectModel projectModel)
        {
            for (ResourceModel childFile : fileModel.getFilesInDirectory())
            {
                if (childFile.getProjectModel() == null)
                {
                    projectModel.addResourceModel(childFile);
                    childFile.setProjectModel(projectModel);
                }
                else if (childFile.getProjectModel().getParentProject() == null && !childFile.getProjectModel().equals(projectModel))
                {
                    // if the child has a project, but the project doesn't have a parent, associate it with the root
                    // project
                    childFile.getProjectModel().setParentProject(projectModel);
                }
                addProjectToChildFiles(childFile, projectModel);
            }
        }

        public String toString()
        {
            return "ScanAsNonMavenProject";
        }
    }

    @Override
    public String toString()
    {
        return "AddProjectInformation";
    }
}
