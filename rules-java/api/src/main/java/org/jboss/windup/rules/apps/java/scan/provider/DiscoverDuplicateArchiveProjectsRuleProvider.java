package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.DuplicateProjectModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.Service;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This creates {@link DuplicateProjectModel}s, associates them with the {@link DuplicateArchiveModel}s
 * and attaches them to the canonical {@link ProjectModel}s.
 * <p>
 * The links between the duplicated projects and archives follow this schema:
 * <pre>
 *    ArchiveX.jar          <->  canonical archive  <-> ArchiveXDuplicate.jar
 *         |                              |                      |
 *    getProjectModel()            getProjectModel()       getProjectModel()
 *         |                              |                      |
 *         V                              V                      V
 *   ArchiveX.jar's project <->  canonical project  <-> ArchiveXDuplicate.jar's project
 * </pre>
 * Canonical (virtual) project and archive has 1:N relation to duplicated project and archives.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = DiscoverMavenHierarchyRuleProvider.class)
public class DiscoverDuplicateArchiveProjectsRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(DuplicateArchiveModel.class))
                .perform(new AbstractIterationOperation<DuplicateArchiveModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, DuplicateArchiveModel payload) {
                        setupProject(event, payload);
                    }
                });
    }

    private void setupProject(GraphRewrite event, DuplicateArchiveModel duplicateArchive) {
        Service<DuplicateProjectModel> duplicateProjectService = event.getGraphContext().service(DuplicateProjectModel.class);
        ArchiveModel canonicalArchive = duplicateArchive.getCanonicalArchive();

        ProjectModel canonicalProject = canonicalArchive.getProjectModel();

        DuplicateProjectModel duplicateProject = duplicateProjectService.create();
        duplicateProject.setCanonicalProject(canonicalProject);
        duplicateProject.setName(canonicalProject.getName());
        if (duplicateArchive.getParentArchive() != null)
            duplicateProject.setParentProject(duplicateArchive.getParentArchive().getProjectModel());
        duplicateProject.setRootFileModel(duplicateArchive);

        if (canonicalProject.getParentProject() == null) {
            ProjectService projectService = new ProjectService(event.getGraphContext());
            ProjectModel sharedLibsProject = projectService.getOrCreateSharedLibsProject();
            canonicalProject.setParentProject(sharedLibsProject);
        }

        duplicateProject.addFileModel(duplicateArchive);
    }
}
