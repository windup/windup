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
        StringBuilder builder = new StringBuilder();
        if (rule instanceof Context)
        {
            final Context ctx = (Context) rule;
            WindupRuleProvider ruleProvider = (WindupRuleProvider) ctx.get(RuleMetadata.RULE_PROVIDER);
            if (ruleProvider != null)
            {
                builder.append(ruleProvider.getPhase()).append(" - ");
                builder.append(ruleProvider.getID()).append(" ");
            }

            String category = (String) ctx.get(RuleMetadata.CATEGORY);
            if (category != null)
                builder.append("[").append(category).append("] ");
        }

        return builder.append(rule.getId()).toString();
    }
    

}// class
