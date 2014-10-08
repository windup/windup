
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.metadata.RuleMetadata
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

blacklistType("sampleRegexBlackListRule-001", "org.apache.wicket.request.handler.logger.NoLogData", "No Log Data")
blacklistType("sampleRegexBlackListRule-002", "org.apache.wicket.request.IRequestHandler", "IRequestHandler Blacklisted")
blacklistType("sampleRegexBlackListRule-003", "org.apache.wicket.util.resource.UrlResourceStream", "UrlResourceStream blacklisted")
blacklistType("sampleRegexBlackListRule-004", "org.apache.wicket.Component", "Wicket Component")
blacklistType("sampleRegexBlackListRule-005", "org.apache.wicket.ComponentEventSender", "Event Sender")
blacklistType("sampleRegexBlackListRule-006", "org.apache.wicket.Behaviors", "Behaviors")
blacklistType("sampleRegexBlackListRule-007", "org.apache.wicket.WicketRuntimeException", "Wicket Runtime Exception")
blacklistType("sampleRegexBlackListRule-008", "org.apache.wicket.IResourceFactory", "Resource Factory")
blacklistType("sampleRegexBlackListRule-009", "org.apache.wicket.Application", "Wicket Application")
blacklistType("sampleRegexBlackListRule-010", "org.apache.wicket.model.IComponentAssignedModel", "Component Assigned Model")
blacklistType("sampleRegexBlackListRule-011", "org.apache.wicket.model.IModelComparator", "Model Comparator")
blacklistType("sampleRegexBlackListRule-012", "javax.servlet.annotation.WebServlet", "Web Servlet")
blacklistType("sampleRegexBlackListRule-013", ".*WebServlet.*", "Web Servlet again")



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
    
