package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Arrays;
import java.util.List;

@RuleMetadata(phase = PostReportGenerationPhase.class, perform = "Add technology tags from technology-identified")
public class DiscoverTechnologyIdentifiedRuleProvider extends AbstractRuleProvider {
    List<String> technologies = Arrays.asList("Stateful (SFSB)", "EJB", "Entity Bean", "JAX-RS", "JAX-WS", "JCA", "JDBC Datasources", "JDBC XA datasources" , "JMS Connection Factory", "JMS Queue", "JMS Topic", "JPA Entities", "JPA named queries", "JSF Page", "JSP Page", "JTA" , "Message (MDB)", "Persistence units", "RMI", "Stateful (SFSB)", "Stateless (SLSB)");
    
    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                .begin()
                .addRule()
                .when(Query.fromType(TechnologyUsageStatisticsModel.class))
                .perform(new AbstractIterationOperation<TechnologyUsageStatisticsModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, TechnologyUsageStatisticsModel payload)
                    {
                        extractMetadata(event, context, payload);
                    }
                })
                .withId(ruleIDPrefix + "_technologyIdentifiedRuleProvider");
    }

    private void extractMetadata(GraphRewrite event, EvaluationContext context, TechnologyUsageStatisticsModel payload) {
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        if (technologies.contains(payload.getName())) {
            technologyTagService.addTagToFileModel(payload.getProjectModel().getRootFileModel(), payload.getName(), TechnologyTagLevel.INFORMATIONAL);
        }
    }
}
