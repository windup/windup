import org.ocpsoft.rewrite.context.EvaluationContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;

registerRegexBlackList("sampleRegexBlackListRule-001", "org.apache.wicket.util.string.CssUtils", "You shouldn't do that")

buildWindupRule("ExampleBlacklistRule")
    .setPhase(RulePhase.MIGRATION_RULES)
    .addRule()
    .when(
        GraphSearchConditionBuilder.create("javaClasses")
            .ofType(JavaClassModel.class)
    )
    .perform(
        Iteration.over("javaClasses").var("javaClass").perform(
            new GraphOperation  () {
                public void perform(GraphRewrite event, EvaluationContext context) {
                    System.out.println("Performing rewrite operation")
                }
            }
        )
    )
    