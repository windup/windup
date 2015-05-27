package org.jboss.windup.exec.test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
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
 * Test for the tags include/exclude - RuleProvider execution filtering based on tags.
 *
 * How this tests works:
 *
 * The 3 RuleProviders have different tags. There are 4 executions, each time with different include/exclude tags.
 * Through the RuleExecutionListener, execution of rules is observed, and the same listener, at the end of execution,
 * checks whether the right set of rules was executed.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RunWith(Arquillian.class)
public class TagsIncludeExcludeTest
{

    public static final String TEST_RULES_THAT_SHOULD_RUN = "test:rulesThatShouldRun";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:windup-utils"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec")
                    );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    public static class TestTagsRuleExecutionListener extends AbstractRuleLifecycleListener
    {
        Map<String, Boolean> executedRules = new HashMap<>();

        @Override
        public void beforeExecution(GraphRewrite event)
        {
            event.getRewriteContext().put("testData", new HashMap<>());
        }

        @Override
        public void beforeRuleEvaluation(GraphRewrite event, Rule rule, EvaluationContext context)
        {
            RuleProvider provider = (RuleProvider) ((Context) rule).get(RuleMetadataType.RULE_PROVIDER);
            String realName = Proxies.unwrapProxyClassName(provider.getClass());
            executedRules.put(realName, Boolean.FALSE);
        }

        @Override
        public void ruleEvaluationProgress(GraphRewrite event, String name, int currentPosition, int total, int timeRemainingInSeconds)
        {
        }

        @Override
        public void afterRuleConditionEvaluation(GraphRewrite event, EvaluationContext context, Rule rule, boolean result)
        {
            RuleProvider provider = (RuleProvider) ((Context) rule).get(RuleMetadataType.RULE_PROVIDER);
            String realName = Proxies.unwrapProxyClassName(provider.getClass());
            executedRules.put(realName, Boolean.TRUE);
        }

        @Override
        public void afterExecution(GraphRewrite event)
        {
            @SuppressWarnings("unchecked")
            Set<Class<? extends RuleProvider>> shouldHaveRun =
                        (Set<Class<? extends RuleProvider>>) event.getGraphContext().getOptionMap().get(TEST_RULES_THAT_SHOULD_RUN);
            assertRule(TestTagsA1B1Rules.class, shouldHaveRun);
            assertRule(TestTagsARules.class, shouldHaveRun);
            assertRule(TestTagsBRules.class, shouldHaveRun);
        }

        private void assertRule(Class<? extends RuleProvider> cls, Set<Class<? extends RuleProvider>> shouldHaveRun)
        {
            final Boolean didItRun = BooleanUtils.isTrue(executedRules.get(cls.getName()));
            Assert.assertEquals(cls.getSimpleName(), shouldHaveRun.contains(cls), didItRun);
        }
    }

    @Test
    public void testIncludeA1Tags()
    {
        executeTest("tagA1", null, new HashSet<Class<? extends RuleProvider>>(Arrays.asList(TestTagsARules.class, TestTagsA1B1Rules.class)));
    }

    @Test
    public void testExcludeA1Tags()
    {
        executeTest(null, "tagA1", new HashSet<Class<? extends RuleProvider>>(Arrays.asList(TestTagsBRules.class)));
    }

    @Test
    public void testCombinedA1B1Tags()
    {
        executeTest("tagA1", "tagB1", new HashSet<Class<? extends RuleProvider>>(Arrays.asList(TestTagsARules.class)));
    }

    @Test
    public void testNoTags()
    {
        /*
         * All rules should be executed (tags should create no limitation).
         */
        executeTest(null, null,
                    new HashSet<Class<? extends RuleProvider>>(Arrays.asList(TestTagsA1B1Rules.class, TestTagsARules.class, TestTagsBRules.class)));
    }

    private void executeTest(String includeTags, String excludeTags, Set<Class<? extends RuleProvider>> rules)
    {
        Set<String> included = null;
        if (includeTags != null)
            included = new HashSet<>(Arrays.asList(StringUtils.split(includeTags)));

        Set<String> excluded = null;
        if (excludeTags != null)
            excluded = new HashSet<>(Arrays.asList(StringUtils.split(excludeTags)));

        try (GraphContext context = contextFactory.create())
        {
            runRules(context, included, excluded, rules);
        }
        catch (Exception ex)
        {
            throw new WindupException(ex.getMessage(), ex);
        }
    }

    /**
     * Configure the WindupConfiguration according to the params and run the RuleProviders.
     */
    private void runRules(GraphContext grCtx, Set<String> inTags, Set<String> exTags,
                Set<Class<? extends RuleProvider>> rules)
    {
        WindupConfiguration wc = new WindupConfiguration();
        wc.setGraphContext(grCtx);
        wc.setInputPath(Paths.get("."));
        wc.setOutputDirectory(Paths.get("target/WindupReport"));

        wc.setOptionValue(IncludeTagsOption.NAME, inTags);
        wc.setOptionValue(ExcludeTagsOption.NAME, exTags);
        wc.setOptionValue(TEST_RULES_THAT_SHOULD_RUN, rules);

        processor.execute(wc);
    }

    @RuleMetadata(tags = { "tagA1", "tagA2", "tagA3" })
    public static class TestTagsARules extends NoopRuleProvider
    {
    }

    @RuleMetadata(tags = { "tagB1", "tagB2", "tagB3" })
    public static class TestTagsBRules extends NoopRuleProvider
    {
    }

    @RuleMetadata(tags = { "tagA1", "tagB1" })
    public static class TestTagsA1B1Rules extends NoopRuleProvider
    {
    }

    public abstract static class NoopRuleProvider extends AbstractRuleProvider
    {
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin().addRule()
                        .perform(Log.message(Logger.Level.TRACE, "Performing Rule: " + this.getClass().getSimpleName()));
        }
    }

}
