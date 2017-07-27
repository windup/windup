package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.File;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.phase.PostFinalizePhase;
import org.jboss.windup.config.phase.PostReportRenderingPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.WindupConfigurationQuery;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.scan.operation.DeleteWorkDirsOperation;
import org.jboss.windup.util.Util;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Not;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Deletes the directories with the content unzipped from the archives.
 * The graph can't be deleted at this point so that's left up to the Bootstrap.
 *
 * @author Ondrej Zizka
 */
@RuleMetadata(
    after = {PostReportRenderingPhase.class},
    // I don't want to create a dependency: before = {ExecutionTimeReportRuleProvider.class},
    description = "Deletes the temporary data " + Util.WINDUP_BRAND_NAME_ACRONYM + " analyzes: the unzipped archives, and the graph data."
            + " Use --" + KeepWorkDirsOption.NAME + " to keep them.",
    phase = PostFinalizePhase.class
)
public class DeleteWorkDirsAtTheEndRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        // @formatter:off
        return ConfigurationBuilder.begin()
        .addRule()
        .when(
                Not.any(WindupConfigurationQuery.hasOption(WindupConfigurationModel.KEEP_WORKING_DIRECTORIES, true).as("discard")),
                Query.fromType(ArchiveModel.class).withProperty(ArchiveModel.UNZIPPED_DIRECTORY).as("archives")
        )
        .perform(
                Iteration.over("archives").perform(
                        DeleteWorkDirsOperation.delete(),
                        IterationProgress.monitoring("Deleted archive unzip directory", 1)
                ).endIteration()
        )
        .addRule().perform(
                        new GraphOperation() {
                            public void perform(GraphRewrite event, EvaluationContext context) {
                                File archivesDir = WindupConfigurationService.getArchivesPath(event.getGraphContext()).toFile();
                                if (archivesDir.exists() && archivesDir.isDirectory() && archivesDir.list().length == 0)
                                    archivesDir.delete();
                            }

                            public String toString() {
                                return "Delete archives directory if empty";
                            }
                        }
                )
        ;
        // @formatter:on
    }

}
