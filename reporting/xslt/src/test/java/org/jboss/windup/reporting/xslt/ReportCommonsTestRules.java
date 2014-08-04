package org.jboss.windup.reporting.xslt;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ReportCommonsTestRules extends WindupRuleProvider
{

    @Inject
    private Furnace furnace;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORT_RENDERING;
    }

    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(
            new ReportCommonsTestOperation()
        );
    }


    
    /**
     * 1) Loads all ReportCommonsModelModel's which are @ReportElement(type=BOX)
     * 2) For each, renders it to a XML snippet, saves to a temp file, stores the path to the vertex
     * 3) Joins these snippets into several XML documents, future report parts.
     */
    private static class ReportCommonsTestOperation extends GraphOperation {
        public void perform( GraphRewrite event, EvaluationContext context ) {
            //event.getGraphContext().getFramed().query().
        }
    }
}
