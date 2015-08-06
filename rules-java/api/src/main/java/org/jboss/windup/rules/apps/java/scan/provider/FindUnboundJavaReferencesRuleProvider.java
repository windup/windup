package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.HasHint;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Finds references to Java classes that were not found in the application and also not in the target environment.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:dynawest@gmail.com">Ondrej Zizka</a>
 */
public class FindUnboundJavaReferencesRuleProvider extends AbstractRuleProvider
{
    public static final String JAVA = "java";
    public static final String RULE_ID = FindUnboundJavaReferencesRuleProvider.class.getSimpleName();
    public static final String TITLE = "Unresolved Class Binding";

    public FindUnboundJavaReferencesRuleProvider()
    {
        super(MetadataBuilder.forProvider(FindUnboundJavaReferencesRuleProvider.class).addTag(JAVA)
                    .setPhase(DependentPhase.class)
                    .addExecuteAfter(PostMigrationRulesPhase.class)
                    .addExecuteBefore(PreReportGenerationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new AttachHintOperation())
                .withId(RULE_ID);
    }
    // @formatter:on

    private class AttachHintOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context)
        {
            // Has hint filter (so that we only add to those that do not have a hint)
            HasHint hasHint = new HasHint();

            // Reuse the progress and commit logic from these
            GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(event.getGraphContext().getGraph());
            pipeline.V();
            pipeline.has(WindupVertexFrame.TYPE_PROP, JavaTypeReferenceModel.TYPE);
            pipeline.hasNot(JavaTypeReferenceModel.RESOLUTION_STATUS, ResolutionStatus.RESOLVED);

            GraphService<JavaTypeReferenceModel> typeReferenceService = new GraphService<>(event.getGraphContext(), JavaTypeReferenceModel.class);
            int count = 0;

            for (Vertex vertex : pipeline)
            {
                JavaTypeReferenceModel typeReference = typeReferenceService.frame(vertex);
                if (hasHint.evaluate(event, context, typeReference))
                {
                    // we already have hints, so this would likely be redundant
                    continue;
                }

                InlineHintModel hint = new InlineHintService(event.getGraphContext()).create();
                hint.setRuleID(RULE_ID);
                hint.setLineNumber(typeReference.getLineNumber());
                hint.setColumnNumber(typeReference.getColumnNumber());
                hint.setLength(typeReference.getLength());
                hint.setFileLocationReference(typeReference);
                hint.setFile(typeReference.getFile());
                hint.setEffort(5);
                hint.setSeverity(Severity.MANDATORY);
                hint.setTitle(TITLE);
                hint.setHint("This class reference (" + typeReference.getDescription() + ") could not be found on the classpath");

                if (typeReference.getFile() instanceof SourceFileModel)
                    ((SourceFileModel) typeReference.getFile()).setGenerateSourceReport(true);

                count++;
                if (count % 1000 == 0)
                {
                    event.getGraphContext().getGraph().getBaseGraph().commit();
                }
            }
        }
    }
}
