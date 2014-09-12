package org.jboss.windup.rules.apps.java.blacklist;

import groovy.lang.Closure;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.builder.WindupRuleProviderBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.ext.groovy.GroovyConfigContext;
import org.jboss.windup.ext.groovy.GroovyConfigMethod;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.reporting.JavaHint;

public class GroovyBlackListMethod implements GroovyConfigMethod
{
    @Override
    public String getName(GroovyConfigContext context)
    {
        return "blacklistType";
    }

    @Override
    public Closure<?> getClosure(final GroovyConfigContext context)
    {
        return new Closure<Void>(this)
        {
            private static final long serialVersionUID = -4738073793316064882L;

            @Override
            public Void call(Object... args)
            {
                String ruleID = (String) args[0];
                String regexPattern = (String) args[1];
                String hint = (String) args[2];

                WindupRuleProviderBuilder ruleProvider = WindupRuleProviderBuilder.begin(ruleID);

                ruleProvider.setPhase(RulePhase.MIGRATION_RULES)
                            .addRule()
                            .when(JavaClass.references(regexPattern).as("refs"))
                            .perform(Iteration.over("refs")
                                        .as("ref")
                                        .perform(JavaHint
                                                    .withText(hint)
                                                    .withEffort(8)
                                        )
                                        .endIteration()
                            );

                context.addRuleProvider(ruleProvider);
                return null;
            }
        };
    }
}
