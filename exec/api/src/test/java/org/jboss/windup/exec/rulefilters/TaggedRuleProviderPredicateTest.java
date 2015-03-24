package org.jboss.windup.exec.rulefilters;

import java.util.Arrays;
import java.util.HashSet;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TaggedRuleProviderPredicateTest
{

    public TaggedRuleProviderPredicateTest()
    {
    }

    @Test
    public void testAccept()
    {
        // MetadataBuilder.forProvider(TestTagsA1B1Rules.class);
        TestTagsARules rulesA = new TestTagsARules();
        TestTagsBRules rulesB = new TestTagsBRules();
        TestTagsA1B1Rules rulesA1B1 = new TestTagsA1B1Rules();

        // inclTags, exclTags, ruleP, reqIncl, reqAllExcl
        Assert.assertEquals(true, tryFilter("tagA1", null, rulesA, false, false));
        Assert.assertEquals(false, tryFilter("tagA1", null, rulesB, false, false));
        Assert.assertEquals(true, tryFilter("tagA1 tagB1", null, rulesB, false, false));
        Assert.assertEquals(false, tryFilter("tagA1 tagB1", null, rulesB, true, false));
        Assert.assertEquals(true, tryFilter("tagA1 tagB1 tagC1", null, rulesB, false, false));
        Assert.assertEquals(false, tryFilter("tagA1 tagB1 tagC1", null, rulesB, true, false));
        Assert.assertEquals(true, tryFilter("tagA1 tagB1", "tagC1", rulesA1B1, false, false));
        Assert.assertEquals(false, tryFilter("tagA1 tagC1", "tagB1", rulesA1B1, false, false));
        Assert.assertEquals(false, tryFilter("tagA1", "tagB1 tagC1", rulesA1B1, false, false));
        Assert.assertEquals(true, tryFilter("tagA1", "tagB1 tagC1", rulesA1B1, false, true));
    }

    private boolean tryFilter(String includeTags, String excludeTags, RuleProvider ruleProvider, boolean requireAllIncludes,
                boolean requireAllExcludes)
    {
        final HashSet inclSet = includeTags == null ? null : new HashSet(Arrays.asList(includeTags.split("\\s+")));
        final HashSet exclSet = excludeTags == null ? null : new HashSet(Arrays.asList(excludeTags.split("\\s+")));
        TaggedRuleProviderPredicate filter = new TaggedRuleProviderPredicate(inclSet, exclSet);
        filter.setRequireAllIncludeTags(requireAllIncludes);
        filter.setRequireAllExcludeTags(requireAllExcludes);
        return filter.accept(ruleProvider);
    }

    // -- RuleProviders --

    // Formatter:off
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
    // Formatter:off

}
