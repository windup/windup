package org.jboss.windup.exec.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.graph.GraphContext;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class TagsRuleProviderFilterTest
{

    public TagsRuleProviderFilterTest()
    {
    }

    @Test
    public void testAccept()
    {
        TagsRuleProviderFilter filterA1 = new TagsRuleProviderFilter(new HashSet(Arrays.asList("tagA1")), null);
        TagsRuleProviderFilter filterA1B2 = new TagsRuleProviderFilter(new HashSet(Arrays.asList("tagA1", "tagB2")), null);

        //MetadataBuilder.forProvider(TestTagsA1B1Rules.class);
        TestTagsARules rulesA = new TestTagsARules();
        TestTagsBRules rulesB = new TestTagsBRules();
        TestTagsA1B1Rules rulesA1B1 = new TestTagsA1B1Rules();

        //                            inclTags,          exclTags,        ruleP,    reqIncl, reqAllExcl
        assertEquals(true,  tryFilter("tagA1",              null,         rulesA,    false, false));
        assertEquals(false, tryFilter("tagA1",              null,         rulesB,    false, false));
        assertEquals(true,  tryFilter("tagA1 tagB1",        null,         rulesB,    false, false));
        assertEquals(false, tryFilter("tagA1 tagB1",        null,         rulesB,    true,  false));
        assertEquals(true,  tryFilter("tagA1 tagB1 tagC1",  null,         rulesB,    false, false));
        assertEquals(false, tryFilter("tagA1 tagB1 tagC1",  null,         rulesB,    true,  false));
        assertEquals(true,  tryFilter("tagA1 tagB1",       "tagC1",       rulesA1B1, false, false));
        assertEquals(false, tryFilter("tagA1 tagC1",       "tagB1",       rulesA1B1, false, false));
        assertEquals(false, tryFilter("tagA1",             "tagB1 tagC1", rulesA1B1, false, false));
        assertEquals(true,  tryFilter("tagA1",             "tagB1 tagC1", rulesA1B1, false, true));
    }

    private boolean tryFilter(String includeTags, String excludeTags, RuleProvider ruleProvider, boolean requireAllIncludes, boolean requireAllExcludes)
    {
        final HashSet inclSet = includeTags == null ? null : new HashSet(Arrays.asList(includeTags.split("\\s+")));
        final HashSet exclSet = excludeTags == null ? null : new HashSet(Arrays.asList(excludeTags.split("\\s+")));
        TagsRuleProviderFilter filter = new TagsRuleProviderFilter(inclSet, exclSet);
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
            .perform(
                Log.message(org.ocpsoft.logging.Logger.Level.TRACE, "Performing Rule: " + this.getClass().getSimpleName())
            );
        }
    }
    // Formatter:off

}
