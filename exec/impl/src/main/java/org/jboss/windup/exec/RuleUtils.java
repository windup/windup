package org.jboss.windup.exec;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;


/**
 * Utils for the Rules. Will be likely moved to Windup Utils.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class RuleUtils {

    /**
     * Describes given rule as "provider-phase - provider-ID [categories...] rule-ID".
     */
    public static String prettyPrintRule(Rule rule)
    {
        StringBuilder sb = new StringBuilder();
        if (rule instanceof Context)
        {
            final Context ctx = (Context) rule;
            WindupRuleProvider ruleProvider = (WindupRuleProvider) ctx.get(RuleMetadata.RULE_PROVIDER);
            if (ruleProvider != null)
            {
                sb.append(ruleProvider.getPhase()).append(" - ");
                sb.append(ruleProvider.getID()).append(' ');
            }

            Object categories = ctx.get(RuleMetadata.CATEGORY);
            if (categories instanceof String )
                sb.append('[').append(categories).append("] ");
            else if (categories instanceof Iterable)
            {
                sb.append('[');
                for(Object cat : (Iterable) categories)
                    sb.append(cat).append(", ");
                sb.append("] ");
            }
        }

        return sb.append(rule.getId()).toString();
    }

}