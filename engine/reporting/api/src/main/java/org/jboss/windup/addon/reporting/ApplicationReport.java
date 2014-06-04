package org.jboss.windup.addon.reporting;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationReport extends GraphOperation
{
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReport.class);
    
    public static ApplicationReport create() {
        return new ApplicationReport();
    }
    
    public ApplicationReport applicationName(String applicationName) {
        return this;
    }
    public ApplicationReport applicationVersion(String applicationVersion) {
        return this;
    }
    public ApplicationReport applicationCreator(String applicationCreator) {
        return this;
    }
    
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        LOG.info("Should perform application report.");
    }
}
