package org.jboss.windup.rules.apps.java.config;

import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.Initialization;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Copies configuration data from {@link GraphContext#getOptionMap()} to the graph itself for easy use by other {@link Rule}s.
 *
 */
public class CopyJavaConfigToGraphRuleProvider extends WindupRuleProvider
{
    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Initialization.class;
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
