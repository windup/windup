package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.scan.operation.AddClassFileMetadata;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Discovers .class files from the applications being analyzed.
 * 
 */
public class IndexJavaClassFilesRuleProvider extends AbstractRuleProvider
{
    public IndexJavaClassFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(IndexJavaClassFilesRuleProvider.class)
                    .setPhase(ClassifyFileTypesPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(JavaClassFileModel.class))
                    .perform(
                        new AddClassFileMetadata()
                        .and(Commit.every(10))
                        .and(IterationProgress.monitoring("Index Class Files", 1000))
                    );
    }
    // @formatter:on
}
