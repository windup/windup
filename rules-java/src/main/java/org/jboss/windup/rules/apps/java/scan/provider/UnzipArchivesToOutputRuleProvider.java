package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.phase.ArchiveExtraction;
import org.jboss.windup.config.phase.RulePhase;
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
 * @author jsightler
 *
 */
public class UnzipArchivesToOutputRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ArchiveExtraction.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin().addRule()
                    .when(Query.fromType(ArchiveModel.class).excludingType(IgnoredArchiveModel.class))
                    .perform(UnzipArchiveToOutputFolder.unzip()
                                .and(IterationProgress.monitoring("Unzipped archive: ", 1))
                                .and(Commit.every(1))
                    );
    }
}
