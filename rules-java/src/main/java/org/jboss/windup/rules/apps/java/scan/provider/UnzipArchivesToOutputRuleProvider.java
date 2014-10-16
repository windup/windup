package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.UnzipArchiveToOutputFolder;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class UnzipArchivesToOutputRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverFileTypesRuleProvider.class);
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(DiscoverXmlFilesRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin().addRule()
            .when(Query.find(ArchiveModel.class))
            .perform(UnzipArchiveToOutputFolder.unzip()
            .and(IterationProgress.monitoring("Unzipped archive: ", 1))
            );
    }
    // @formatter:on
}
