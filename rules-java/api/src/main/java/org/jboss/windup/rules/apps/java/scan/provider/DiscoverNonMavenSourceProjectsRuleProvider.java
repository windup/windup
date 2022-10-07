package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
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
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = DiscoverNonMavenArchiveProjectsRuleProvider.class)
public class DiscoverNonMavenSourceProjectsRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new AddProjectInformation());
    }

    private class AddProjectInformation extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            for (FileModel mainFileModel : configuration.getInputPaths()) {
                ProjectService projectModelService = new ProjectService(event.getGraphContext());
                ProjectModel mainProjectModel = mainFileModel.getProjectModel();
                if (mainProjectModel == null) {
                    mainProjectModel = projectModelService.create();
                    mainProjectModel.setName(mainFileModel.getFileName());
                    mainProjectModel.setDescription("Source Directory");

                    mainProjectModel.setRootFileModel(mainFileModel);
                    mainProjectModel.addFileModel(mainFileModel);
                }

                addProjectToChildFiles(mainFileModel, mainProjectModel);
            }
        }

        private void addProjectToChildFiles(FileModel fileModel, ProjectModel projectModel) {
            for (FileModel childFile : fileModel.getFilesInDirectory()) {
                boolean childHasProject = childFile.getProjectModel() != null;
                // Also, if it is a duplicate, check the canonical archive
                if (!childHasProject && childFile instanceof DuplicateArchiveModel)
                    childHasProject = ((DuplicateArchiveModel) childFile).getCanonicalArchive().getProjectModel() != null;

                if (!childHasProject) {
                    projectModel.addFileModel(childFile);
                } else if (childFile.getProjectModel() != null && childFile.getProjectModel().getParentProject() == null && !childFile.getProjectModel().equals(projectModel)) {
                    // if the child has a project, but the project doesn't have a parent, associate it with the root
                    // project
                    childFile.getProjectModel().setParentProject(projectModel);
                }
                addProjectToChildFiles(childFile, projectModel);
            }
        }

        public String toString() {
            return "ScanAsNonMavenProject";
        }
    }

    @Override
    public String toString() {
        return "AddProjectInformation";
    }
}
