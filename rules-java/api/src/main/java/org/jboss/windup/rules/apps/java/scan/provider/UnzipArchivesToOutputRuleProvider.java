package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.UnzipArchiveToOutputFolder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Unzip archives from the input application.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class UnzipArchivesToOutputRuleProvider extends AbstractRuleProvider
{
    public UnzipArchivesToOutputRuleProvider()
    {
        super(MetadataBuilder.forProvider(UnzipArchivesToOutputRuleProvider.class)
                    .setPhase(ArchiveExtractionPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin().addRule()
                    .when(Query.fromType(ArchiveModel.class).excludingType(IgnoredArchiveModel.class))
                    .perform(UnzipArchiveToOutputFolder.unzip()
                                .and(IterationProgress.monitoring("Unzipped archive", 1))
                                .and(Commit.every(1))
                    );
    }
}
