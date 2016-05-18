package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.DuplicateProjectModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = DiscoverMavenHierarchyRuleProvider.class)
public class DiscoverDuplicateArchiveProjectsRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(DuplicateArchiveModel.class))
                .perform(new AbstractIterationOperation<DuplicateArchiveModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, DuplicateArchiveModel payload)
                    {
                        setupProject(event, payload);
                    }
                });
    }

    private void setupProject(GraphRewrite event, DuplicateArchiveModel duplicateArchive)
    {
        GraphService<DuplicateProjectModel> duplicateProjectService = event.getGraphContext().service(DuplicateProjectModel.class);

        ProjectModel originalProject = duplicateArchive.getOriginalArchive().getProjectModel();

        DuplicateProjectModel duplicateProject = duplicateProjectService.create();
        duplicateProject.setOriginalProject(originalProject);
        duplicateProject.setName(originalProject.getName());
        duplicateProject.setParentProject(duplicateArchive.getParentArchive().getProjectModel());
        duplicateProject.setRootFileModel(duplicateArchive);

        if (originalProject.getParentProject() == null)
        {
            ProjectService projectService = new ProjectService(event.getGraphContext());
            ProjectModel sharedLibsProject = projectService.getSharedLibsProject();
            originalProject.setParentProject(sharedLibsProject);
        }

        duplicateProject.addFileModel(duplicateArchive);
    }
}
