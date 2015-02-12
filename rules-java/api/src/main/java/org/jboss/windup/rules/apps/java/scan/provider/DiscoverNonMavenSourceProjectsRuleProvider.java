package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructure;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds files that were not classified as Maven archives/projects, and adds some generic project information for them.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class DiscoverNonMavenSourceProjectsRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return DiscoverProjectStructure.class;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverNonMavenArchiveProjectsRuleProvider.class);
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
            FileModel mainFileModel = cfg.getInputPath();

            ProjectService projectModelService = new ProjectService(event.getGraphContext());
            ProjectModel mainProjectModel = mainFileModel.getProjectModel();
            if (mainProjectModel == null)
            {
                mainProjectModel = projectModelService.create();
                mainProjectModel.setName(mainFileModel.getFileName());
                mainProjectModel.setDescription("Source Directory");

                mainFileModel.setProjectModel(mainProjectModel);
                mainProjectModel.setRootFileModel(mainFileModel);
                mainProjectModel.addFileModel(mainFileModel);
            }

            addProjectToChildFiles(mainFileModel, mainProjectModel);
        }

        private void addProjectToChildFiles(FileModel fileModel, ProjectModel projectModel)
        {
            for (FileModel childFile : fileModel.getFilesInDirectory())
            {
                if (childFile.getProjectModel() == null)
                {
                    projectModel.addFileModel(childFile);
                    childFile.setProjectModel(projectModel);
                }
                else if (childFile.getProjectModel().getParentProject() == null && !childFile.getProjectModel().equals(projectModel))
                {
                    // if the child has a project, but the project doesn't have a parent, associate it with the root project
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
