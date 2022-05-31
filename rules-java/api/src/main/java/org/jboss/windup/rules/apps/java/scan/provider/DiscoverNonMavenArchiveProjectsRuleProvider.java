package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationFilter;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Finds Archives that were not classified as Maven archives/projects, and adds some generic project information for them.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RuleMetadata(phase = DiscoverProjectStructurePhase.class, after = DiscoverMavenProjectsRuleProvider.class)
public class DiscoverNonMavenArchiveProjectsRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        // @formatter:off
        return ConfigurationBuilder.begin()
                .addRule()
                .when(
                        Query.fromType(ArchiveModel.class)
                ).perform(
                        Iteration.over(ArchiveModel.class)
                                .when(new AbstractIterationFilter<ArchiveModel>() {
                                    @Override
                                    public boolean evaluate(GraphRewrite event, EvaluationContext context, ArchiveModel payload) {
                                        try {
                                            return !(payload instanceof DuplicateArchiveModel) && payload.getProjectModel() == null;
                                        } catch (NoSuchElementException e) {
                                            return true;
                                        }
                                    }

                                    @Override
                                    public String toString() {
                                        return "ProjectModel == null";
                                    }
                                })
                                .perform(
                                        new AbstractIterationOperation<ArchiveModel>() {
                                            @Override
                                            public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload) {
                                                List<ArchiveModel> hierarchy = new ArrayList<>();

                                                ArchiveModel parentArchive = payload;
                                                while (parentArchive != null) {
                                                    hierarchy.add(parentArchive);

                                                    try {
                                                        // break once we have added a parent with a project model
                                                        if (parentArchive.getProjectModel() != null) {
                                                            break;
                                                        }
                                                    } catch (NoSuchElementException e) {
                                                        break;
                                                    }

                                                    parentArchive = parentArchive.getParentArchive();

                                                }

                                                ProjectModel childProjectModel = null;
                                                ProjectService projectModelService = new ProjectService(event.getGraphContext());
                                                for (ArchiveModel archiveModel : hierarchy) {
                                                    ProjectModel projectModel = null;
                                                    try {
                                                        projectModel = archiveModel.getProjectModel();
                                                    } catch (NoSuchElementException e) {
                                                        // just ignore it... just means that it is null
                                                    }

                                                    // create the project if we don't already have one
                                                    if (projectModel == null) {
                                                        projectModel = projectModelService.create();
                                                        projectModel.setName(archiveModel.getArchiveName());
                                                        projectModel.setRootFileModel(archiveModel);
                                                        projectModel.setDescription("Not available");

                                                        if (ZipUtil.endsWithZipExtension(archiveModel.getArchiveName())) {
                                                            for (String extension : ZipUtil.getZipExtensions()) {
                                                                if (archiveModel.getArchiveName().endsWith(extension))
                                                                    projectModel.setProjectType(extension);
                                                            }
                                                        }

                                                        projectModel.addFileModel(archiveModel);
                                                        // Attach the project to all files within the archive
                                                        for (FileModel f : archiveModel.getAllFiles()) {
                                                            // don't add archive models, as those really are separate projects...
                                                            if (f instanceof ArchiveModel)
                                                                continue;

                                                            // also, don't set the project model if one is already set
                                                            // this uses the edge directly to improve performance
                                                            if (f.getElement().vertices(Direction.IN, ProjectModel.PROJECT_MODEL_TO_FILE).hasNext())
                                                                continue;

                                                            // only set it if it has not already been set
                                                            projectModel.addFileModel(f);
                                                        }
                                                    }

                                                    if (childProjectModel != null) {
                                                        childProjectModel.setParentProject(projectModel);
                                                    }
                                                    childProjectModel = projectModel;
                                                }
                                            }

                                            public String toString() {
                                                return "ScanAsNonMavenProject";
                                            }
                                        }
                                                .and(IterationProgress.monitoring("Checking for non-Maven archive", 1))
                                )
                                .endIteration()
                );
        // @formatter:on
    }

}
