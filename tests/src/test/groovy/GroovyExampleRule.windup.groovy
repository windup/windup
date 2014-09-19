import org.ocpsoft.rewrite.context.EvaluationContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.metadata.RuleMetadata
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;


blacklistType("sampleRegexBlackListRule-001", "javax.servlet.annotation.WebServlet", "Web Servlet")
blacklistType("sampleRegexBlackListRule-002", "java.lang.StringBuilder.*", "This is using a StringBuilder")
blacklistType("sampleRegexBlackListRule-003", "java.net.URL.*", "This is using java.net.URL")
blacklistType("sampleRegexBlackListRule-004", "URL.*", "This is using URL")
blacklistType("sampleRegexBlackListRule-005", "java.io.InputStream.*", "This is using java.io.InputStream")
blacklistType("sampleRegexBlackListRule-006", "InputStream.*", "This is using InputStream")
blacklistType("sampleRegexBlackListRule-007", "java.io.OutputStream.*", "This is using java.io.OutputStream")
blacklistType("sampleRegexBlackListRule-008", "OutputStream.*", "This is using OutputStream")


ruleSet("ExampleBlacklistRule").setPhase(RulePhase.MIGRATION_RULES)

    .addRule()
    .when(
        Query.find(JavaClassModel.class).as("javaClasses")
    )
    .perform(
        Iteration.over("javaClasses").perform(
            new GraphOperation  () {
                public void perform(GraphRewrite event, EvaluationContext context) {
                    // System.out.println("Performing rewrite operation")
                }
            }
        )
    )
    .withMetadata(RuleMetadata.CATEGORY, "Java")
    

    