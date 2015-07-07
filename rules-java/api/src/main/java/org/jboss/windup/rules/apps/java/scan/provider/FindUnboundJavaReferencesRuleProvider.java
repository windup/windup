package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.condition.UnresolvedClassCondition;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindUnboundJavaReferencesRuleProvider extends AbstractRuleProvider
{
    public static final String JAVA = "java";
    public static final String RULE_ID = FindUnboundJavaReferencesRuleProvider.class.getSimpleName();

    public FindUnboundJavaReferencesRuleProvider()
    {
        super(MetadataBuilder.forProvider(FindUnboundJavaReferencesRuleProvider.class).addTag(JAVA));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(new UnresolvedClassCondition())
                    .perform(new AttachHintOperation())
                    .withId(RULE_ID);
    }
    // @formatter:on

    private class AttachHintOperation extends AbstractIterationOperation<JavaTypeReferenceModel>
    {
        // @formatter:off
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
        {
            InlineHintModel hint = new InlineHintService(event.getGraphContext()).create();
            hint.setRuleID(RULE_ID);
            hint.setLineNumber(payload.getLineNumber());
            hint.setColumnNumber(payload.getColumnNumber());
            hint.setLength(payload.getLength());
            hint.setFileLocationReference(payload);
            hint.setFile(payload.getFile());
            hint.setEffort(5);
            hint.setSeverity(Severity.MANDATORY);
            hint.setTitle("Unresolved Class Binding");
            hint.setHint("This class reference (" + payload.getDescription() + ") could not be found on the classpath");

            if (payload.getFile() instanceof SourceFileModel)
                ((SourceFileModel) payload.getFile()).setGenerateSourceReport(true);
        }
        // @formatter:on
    }
}
