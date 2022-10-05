package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.service.EjbRemoteServiceModelService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers remote interfaces and marks them for reporting.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = PostMigrationRulesPhase.class, perform = "Mark EJB Remote Class Files")
public class DiscoverRemoteEjbRuleProvider extends IteratingRuleProvider<EjbSessionBeanModel> {
    @Override
    public ConditionBuilder when() {
        return Query.fromType(EjbSessionBeanModel.class);
    }

    public void perform(GraphRewrite event, EvaluationContext context, EjbSessionBeanModel payload) {
        if (payload.getEjbRemote() != null) {
            EjbRemoteServiceModelService service = new EjbRemoteServiceModelService(event.getGraphContext());
            service.getOrCreate(payload.getApplications(), payload.getEjbRemote(), payload.getEjbClass());
        }
    }
}
