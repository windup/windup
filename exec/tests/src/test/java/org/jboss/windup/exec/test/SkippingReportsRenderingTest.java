package org.jboss.windup.exec.test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.phase.PostReportRenderingPhase;
import org.jboss.windup.config.phase.PreReportGenerationPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Test for skipping reports rendering - RuleProvider execution filtering based on report related phases .
 * <p>
 * How this tests works:
 * <p>
 * The 6 RuleProviders have different phase. Execution is configured to disable processing rules in phases related to reports. Through the
 * RuleExecutionListener, execution of rules is observed, and the same listener, at the end of execution, checks whether the right rule with a phase
 * was executed.
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@RunWith(Arquillian.class)
public class SkippingReportsRenderingTest {

    final List<Class<? extends RuleProvider>> RULES = Arrays.asList(TestRuleinPreReportGenerationPhase.class,
            TestRuleinPostReportRendenringPhase.class,
            TestRuleinMigrationRulesPhase.class, TestRuleinPostReportGenerationPhase.class, TestRuleinReportGenerationPhase.class,
            TestRuleinReportRenderingPhase.class);

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    public static class TestPhaseRuleExecutionListener extends AbstractRuleLifecycleListener {
        Map<String, Boolean> executedRules = new HashMap<>();

        @Override
        public void beforeExecution(GraphRewrite event) {
            event.getRewriteContext().put("testData", new HashMap<>());
        }

        @Override
        public boolean beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context) {
            RuleProvider provider = (RuleProvider) ((Context) rule).get(RuleMetadataType.RULE_PROVIDER);
            String realName = Proxies.unwrapProxyClassName(provider.getClass());
            executedRules.put(realName, Boolean.FALSE);
            return false;
        }

        @Override
        public boolean ruleEvaluationProgress(GraphRewrite event, String name, int currentPosition, int total, int timeRemainingInSeconds) {
            return false;
        }

        @Override
        public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result) {
            RuleProvider provider = (RuleProvider) ((Context) rule).get(RuleMetadataType.RULE_PROVIDER);
            String realName = Proxies.unwrapProxyClassName(provider.getClass());
            executedRules.put(realName, Boolean.TRUE);
        }

        @Override
        public void afterExecution(GraphRewrite event) {
            verifyExecutionOfRule(TestRuleinMigrationRulesPhase.class, Boolean.TRUE);
            verifyExecutionOfRule(TestRuleinPreReportGenerationPhase.class, Boolean.FALSE);
            verifyExecutionOfRule(TestRuleinReportGenerationPhase.class, Boolean.FALSE);
            verifyExecutionOfRule(TestRuleinPostReportGenerationPhase.class, Boolean.FALSE);
            verifyExecutionOfRule(TestRuleinReportRenderingPhase.class, Boolean.FALSE);
            verifyExecutionOfRule(TestRuleinPostReportRendenringPhase.class, Boolean.FALSE);

        }

        private void verifyExecutionOfRule(Class<? extends RuleProvider> cls, Boolean shouldHaveRun) {
            final Boolean didItRun = BooleanUtils.isTrue(executedRules.get(cls.getName()));
            Assert.assertEquals(cls.getSimpleName(), shouldHaveRun, didItRun);
        }
    }

    @Test
    public void testRules() {
        executeTest(RULES);
    }

    private void executeTest(List<Class<? extends RuleProvider>> rules) {
        try (GraphContext context = contextFactory.create(true)) {
            runRules(context, rules);
        } catch (Exception ex) {
            throw new WindupException(ex.getMessage(), ex);
        }
    }

    /**
     * Configure the WindupConfiguration according to the params and run the RuleProviders.
     */
    private void runRules(GraphContext context, List<Class<? extends RuleProvider>> rules) {
        WindupConfiguration wc = new WindupConfiguration();
        wc.setGraphContext(context);
        wc.addInputPath(Paths.get("."));
        wc.setOutputDirectory(Paths.get("target/WindupReport"));
        wc.setOptionValue(SkipReportsRenderingOption.NAME, true);

        processor.execute(wc);
    }

    @RuleMetadata(phase = PreReportGenerationPhase.class)
    public static class TestRuleinPreReportGenerationPhase extends NoopRuleProvider {
    }

    @RuleMetadata(phase = ReportGenerationPhase.class)
    public static class TestRuleinReportGenerationPhase extends NoopRuleProvider {
    }

    @RuleMetadata(phase = PostReportGenerationPhase.class)
    public static class TestRuleinPostReportGenerationPhase extends NoopRuleProvider {
    }


    @RuleMetadata(phase = ReportRenderingPhase.class)
    public static class TestRuleinReportRenderingPhase extends NoopRuleProvider {
    }

    @RuleMetadata(phase = PostReportRenderingPhase.class)
    public static class TestRuleinPostReportRendenringPhase extends NoopRuleProvider {
    }

    @RuleMetadata(phase = MigrationRulesPhase.class)
    public static class TestRuleinMigrationRulesPhase extends NoopRuleProvider {
    }


    public abstract static class NoopRuleProvider extends AbstractRuleProvider {
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(Log.message(Logger.Level.TRACE, "Performing Rule: " + this.getClass().getSimpleName()));
        }
    }

}
