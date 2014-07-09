import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.metadata.RuleMetadata
import org.ocpsoft.rewrite.config.True;
import org.ocpsoft.rewrite.context.EvaluationContext;

ruleSet("ExampleGroovyRule").setPhase(RulePhase.MIGRATION_RULES)

    .addRule()
    .when(
        new True()
    )
    .perform(
        new GraphOperation  () {
            public void perform(GraphRewrite event, EvaluationContext context) {
                System.out.println("Performing rewrite operation")
            }
        }
    )
    .withMetadata(RuleMetadata.CATEGORY, "Basic")
    