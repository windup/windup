package org.jboss.windup.rules.apps.java.decompiler;

/**
 * Predecompilation scan - performance purposes.
 */
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

@RuleMetadata(phase = DecompilationPhase.class)
public class BeforeDecompileClassesRuleProvider extends AbstractRuleProvider
{
    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .when(Query.fromType(JavaClassFileModel.class)
                .withoutProperty(FileModel.PARSE_ERROR)
        )
        .perform(new ClassFilePreDecompilationScan());
    }
    // @formatter:on
}
