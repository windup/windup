package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.rules.apps.java.scan.operation.UnzipArchiveToTemporaryFolder;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class UnzipArchivesToTempRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(FileScannerWindupRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder binaryModeOnly = Query.find(WindupConfigurationModel.class)
            .withProperty(WindupConfigurationModel.PROPERTY_SOURCE_MODE, false)
            .as("cfg");

        return ConfigurationBuilder.begin().addRule()
            .when(
                binaryModeOnly.and(Query.find(ArchiveModel.class))
            )
            .perform(Iteration.over(ArchiveModel.class)
                .perform(UnzipArchiveToTemporaryFolder.unzip())
                .endIteration()
            );
    }
    // @formatter:on
}
