package org.jboss.windup.rules.apps.java.config;

import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CopyJavaConfigToGraphRuleProvider extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.PRE_DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GraphOperation copyConfigToGraph = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                Map<String, Object> config = event.getGraphContext().getOptionMap();
                Boolean sourceMode = (Boolean) config.get(SourceModeOption.NAME);
                @SuppressWarnings("unchecked")
                List<String> includeJavaPackages = (List<String>) config.get(ScanPackagesOption.NAME);
                @SuppressWarnings("unchecked")
                List<String> excludeJavaPackages = (List<String>) config.get(ExcludePackagesOption.NAME);

                WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(event
                            .getGraphContext());
                javaCfg.setSourceMode(sourceMode == null ? false : sourceMode);
                javaCfg.setScanJavaPackageList(includeJavaPackages);
                javaCfg.setExcludeJavaPackageList(excludeJavaPackages);
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .perform(copyConfigToGraph);
    }

}
