package org.jboss.windup.exec.rulefilters;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Log;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class TaggedRuleProviderPredicateTest {
    @Test
    public void testAccept() {
        TestTagsARules rulesA = new TestTagsARules();
        TestTagsBRules rulesB = new TestTagsBRules();
        TestTagsA1B1Rules rulesA1B1 = new TestTagsA1B1Rules();

        Assert.assertEquals(true, isProviderAcceptedByTags(rulesA, Arrays.asList("tagA1"), false, null, false));
        Assert.assertEquals(false, isProviderAcceptedByTags(rulesB, Arrays.asList("tagA1"), false, null, false));
        Assert.assertEquals(true, isProviderAcceptedByTags(rulesB, Arrays.asList("tagA1", "tagB1"), false, null, false));
        Assert.assertEquals(false, isProviderAcceptedByTags(rulesB, Arrays.asList("tagA1", "tagB1"), true, null, false));
        Assert.assertEquals(true, isProviderAcceptedByTags(rulesB, Arrays.asList("tagA1", "tagB1", "tagC1"), false, null, false));
        Assert.assertEquals(false, isProviderAcceptedByTags(rulesB, Arrays.asList("tagA1", "tagB1", "tagC1"), true, null, false));
        Assert.assertEquals(true, isProviderAcceptedByTags(rulesA1B1, Arrays.asList("tagA1", "tagB1"), false, Arrays.asList("tagC1"), false));
        Assert.assertEquals(false, isProviderAcceptedByTags(rulesA1B1, Arrays.asList("tagA1", "tagC1"), false, Arrays.asList("tagB1"), false));
        Assert.assertEquals(false, isProviderAcceptedByTags(rulesA1B1, Arrays.asList("tagA1"), false, Arrays.asList("tagB1", "tagC1"), false));
        Assert.assertEquals(true, isProviderAcceptedByTags(rulesA1B1, Arrays.asList("tagA1"), false, Arrays.asList("tagB1", "tagC1"), true));
    }

    private boolean isProviderAcceptedByTags(RuleProvider ruleProvider,
                                             Collection<String> includeTags, boolean requireAllIncludes,
                                             Collection<String> excludeTags, boolean requireAllExcludes) {
        TaggedRuleProviderPredicate filter = new TaggedRuleProviderPredicate(includeTags, excludeTags);
        filter.setRequireAllIncludeTags(requireAllIncludes);
        filter.setRequireAllExcludeTags(requireAllExcludes);
        return filter.accept(ruleProvider);
    }

    @RuleMetadata(tags = {"tagA1", "tagA2", "tagA3"})
    public static class TestTagsARules extends NoopRuleProvider {
    }

    @RuleMetadata(tags = {"tagB1", "tagB2", "tagB3"})
    public static class TestTagsBRules extends NoopRuleProvider {
    }

    @RuleMetadata(tags = {"tagA1", "tagB1"})
    public static class TestTagsA1B1Rules extends NoopRuleProvider {
    }

    public abstract static class NoopRuleProvider extends AbstractRuleProvider {
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin().addRule()
                    .perform(Log.message(Logger.Level.TRACE, "Performing Rule: " + this.getClass().getSimpleName()));
        }
    }

}
