package org.jboss.windup.reporting.service.rule;

import javax.enterprise.context.Dependent;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.rule.RuleProviderModel;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@Dependent
public class RuleProviderService extends GraphService<RuleProviderModel>
{
    public RuleProviderService(GraphContext context)
    {
        super(context, RuleProviderModel.class);
    }
}
