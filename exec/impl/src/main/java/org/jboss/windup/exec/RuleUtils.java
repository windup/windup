package org.jboss.windup.exec;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class RuleUtils {

    public static String prettyPrintRule(Rule rule)
    {
        StringBuilder builder = new StringBuilder();
        if (rule instanceof Context)
        {
            WindupRuleProvider ruleProvider = (WindupRuleProvider) ((Context) rule)
                        .get(RuleMetadata.RULE_PROVIDER);

            String category = (String) ((Context) rule).get(RuleMetadata.CATEGORY);

            if (ruleProvider != null)
            {
                builder.append(ruleProvider.getPhase()).append(" - ");
                builder.append(ruleProvider.getID()).append(" ");
            }

            if (category != null)
                builder.append("[").append(category).append("] ");
        }

        return builder.append(rule.getId()).toString();
    }
    

}// class
