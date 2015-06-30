package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers remote interfaces and marks them for reporting.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class DiscoverRemoteEjbRuleProvider extends IteratingRuleProvider<EjbSessionBeanModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverRemoteEjbRuleProvider.class.getSimpleName());

    public DiscoverRemoteEjbRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverRemoteEjbRuleProvider.class)
                    .setPhase(PostMigrationRulesPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Mark EJB Remote Class Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(EjbSessionBeanModel.class);
    }

    public void perform(GraphRewrite event, EvaluationContext context, EjbSessionBeanModel payload)
    {
        GraphService<EjbRemoteServiceModel> ejbRemoteService = new GraphService<>(event.getGraphContext(), EjbRemoteServiceModel.class);

        if (payload.getEjbRemote() != null)
        {
            EjbRemoteServiceModel remoteModel = ejbRemoteService.create();
            remoteModel.setInterface(payload.getEjbRemote());
            remoteModel.setImplementationClass(payload.getEjbClass());
        }
    }

}
