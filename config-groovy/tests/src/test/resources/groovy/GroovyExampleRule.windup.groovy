import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.True;
import org.ocpsoft.rewrite.context.EvaluationContext;


ruleSet("ExampleGroovyRule").setPhase(MigrationRules.class)

    .addRule()
    .when(
        new True()
    )
    .perform(
        new GraphOperation  () {
            public void perform(GraphRewrite event, EvaluationContext context) {
                Logging.get(this.getClass()).info("Performing rewrite operation in ExampleGroovyRule");
            }
        }
    )
    .withMetadata(RuleMetadata.CATEGORY, "Basic")
    