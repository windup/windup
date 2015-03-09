package org.jboss.windup.exec;

import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;

/**
 * Utils for the Rules. Will be likely moved to Windup Utils.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RuleUtils
{
    /**
     * Describes given rule as:
     * <p>
     * <code>ID: Phase - Provider [tags ...]".</code>
     */
    public static String prettyPrintRule(Rule rule)
    {
        StringBuilder result = new StringBuilder();
        if (rule instanceof Context)
        {
            final Context context = (Context) rule;

            if (rule.getId() != null)
                result.append(rule.getId()).append(": ");
            else
                result.append("Rule: ");

            RuleProvider provider = (RuleProvider) context.get(RuleMetadata.RULE_PROVIDER);
            if (provider != null && provider.getMetadata() != null)
            {
                result.append(provider.getMetadata().getPhase()).append(" - ");
                result.append(provider.getMetadata().getID()).append(' ');
            }

            Object tags = context.get(RuleMetadata.TAGS);
            if (tags != null)
                result.append(tags);
        }

        return result.toString();
    }

}