package org.jboss.windup.rules.apps.java.groovy.blacklist;

import javax.inject.Inject;

import groovy.lang.Closure;

import org.jboss.windup.ext.groovy.GroovyConfigMethod;
import org.jboss.windup.ext.groovy.java.events.GroovyJavaASTEventService;
import org.jboss.windup.graph.GraphContext;

public class GroovyBlackListMethod implements GroovyConfigMethod
{
    @Inject
    private GroovyJavaASTEventService dslSupport;

    @Override
    public String getName(GraphContext context)
    {
        return "blacklistType";
    }

    @Override
    public Closure<?> getClosure(final GraphContext context)
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

                dslSupport.registerInterest(context, ruleID, regexPattern, hint);
                return null;
            }
        };
    }
}
