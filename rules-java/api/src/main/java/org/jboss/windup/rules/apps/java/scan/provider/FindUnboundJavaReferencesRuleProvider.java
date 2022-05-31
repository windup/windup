package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.config.HasHint;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Finds references to Java classes that were not found in the application and also not in the target environment.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:dynawest@gmail.com">Ondrej Zizka</a>
 */
@RuleMetadata(
        phase = DependentPhase.class,
        after = PostMigrationRulesPhase.class,
        before = PreReportGenerationPhase.class,
        tags = FindUnboundJavaReferencesRuleProvider.JAVA)
public class FindUnboundJavaReferencesRuleProvider extends AbstractRuleProvider {
    public static final String JAVA = "java";
    public static final String RULE_ID = FindUnboundJavaReferencesRuleProvider.class.getSimpleName();
    public static final String TITLE = "Unresolved Class Binding";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupJavaConfigurationModel.class).withProperty(WindupJavaConfigurationModel.CLASS_NOT_FOUND_ANALYSIS_ENABLED, true))
                .perform(new AttachHintOperation())
                .withId(RULE_ID);
    }
    // @formatter:on

    private class AttachHintOperation extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context) {
            // Has hint filter (so that we only add to those that do not have a hint)
            HasHint hasHint = new HasHint();

            // Reuse the progress and commit logic from these
            GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(event.getGraphContext().getGraph())
                    .V()
                    .has(WindupVertexFrame.TYPE_PROP, JavaTypeReferenceModel.TYPE)
                    .has(JavaTypeReferenceModel.RESOLUTION_STATUS, P.neq(ResolutionStatus.RESOLVED));

            GraphService<JavaTypeReferenceModel> typeReferenceService = new GraphService<>(event.getGraphContext(), JavaTypeReferenceModel.class);
            int count = 0;

            for (Vertex vertex : pipeline.toList()) {
                JavaTypeReferenceModel typeReference = typeReferenceService.frame(vertex);
                if (hasHint.evaluate(event, context, typeReference)) {
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

                IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
                hint.setIssueCategory(issueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.MANDATORY));
                hint.setTitle(TITLE);
                hint.setHint("This class reference (" + typeReference.getDescription() + ") could not be found on the classpath");

                typeReference.getFile().setGenerateSourceReport(true);

                count++;
                if (count % 1000 == 0) {
                    event.getGraphContext().commit();
                }
            }
        }
    }
}
