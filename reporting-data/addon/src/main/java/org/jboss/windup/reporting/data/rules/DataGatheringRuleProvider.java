package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class DataGatheringRuleProvider extends AbstractRuleProvider {

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        ExecutorService executorService = AbstractApiRuleProvider.executorServiceMap.get(event.getGraphContext());
                        executorService.shutdown();
                        try {
                            executorService.awaitTermination(2, TimeUnit.DAYS);
                        } catch (InterruptedException e) {
                            throw new WindupException("Failed to render reports due to a timeout: " + e.getMessage(), e);
                        } finally {
                            AbstractApiRuleProvider.executorServiceMap.remove(event.getGraphContext());
                        }
                    }
                });
    }
}
