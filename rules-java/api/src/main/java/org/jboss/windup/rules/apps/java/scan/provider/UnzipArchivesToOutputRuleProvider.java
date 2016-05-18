package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.UnzipArchiveToOutputFolder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Unzip archives from the input application.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ArchiveExtractionPhase.class)
public class UnzipArchivesToOutputRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(Query.fromType(ArchiveModel.class).excludingType(IgnoredArchiveModel.class))
            .perform(
                UnzipArchiveToOutputFolder.unzip(),
                IterationProgress.monitoring("Unzipped archive", 1),
                Commit.every(1)
            )
            .addRule()
            .when(Query.fromType(ArchiveModel.class).excludingType(DuplicateArchiveModel.class))
            .perform(new DuplicateArchiveOperation());
    }

    /**
     * Processes {@link ArchiveModel}s and makes sure that any that have duplicates are removed from the tree and
     * replaced with a {@link DuplicateArchiveModel} that links to them.
     */
    private class DuplicateArchiveOperation extends AbstractIterationOperation<ArchiveModel>
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel originalArchive)
        {
            // Skip if there were no duplicates
            if (!originalArchive.getDuplicateArchives().iterator().hasNext())
            {
                return;
            }

            // Get the original archive and remove it from its current position in the tree
            ArchiveModel originalParentArchive = originalArchive.getParentArchive();
            FileModel originalArchiveParentFile = originalArchive.getParentFile();
            originalArchive.setParentFile(null);
            originalArchive.setParentArchive(null);

            /*
             * Create the duplicate archive, link it to the original archive and
             * place it in the tree.
             *
             * Essentially this will replace the original Archive vertex with a vertex that points back to the
             * single canonical source for this archive.
             */
            GraphService<DuplicateArchiveModel> duplicateArchiveService = event.getGraphContext().service(DuplicateArchiveModel.class);
            DuplicateArchiveModel duplicateArchive = duplicateArchiveService.create();
            duplicateArchive.setCanonicalArchive(originalArchive);
            duplicateArchive.setSHA1Hash(originalArchive.getSHA1Hash());
            duplicateArchive.setFilePath(originalArchive.getFilePath());
            duplicateArchive.setArchiveName(originalArchive.getArchiveName());
            duplicateArchive.setFileName(originalArchive.getFileName());
            duplicateArchive.setParentArchive(originalParentArchive);
            duplicateArchive.setParentFile(originalArchiveParentFile);
        }
    }
}
