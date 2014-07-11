package org.jboss.windup.rules.apps.java.blacklist;

import javax.inject.Inject;

import groovy.lang.Closure;

import org.jboss.windup.ext.groovy.GroovyConfigMethod;
import org.jboss.windup.ext.java.events.JavaASTEventService;
import org.jboss.windup.graph.GraphContext;

public class GroovyBlackListMethod implements GroovyConfigMethod
{
    @Inject
    private JavaASTEventService dslSupport;

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
                BlackListRegex blackListSupportRegex = new BlackListRegex(ruleID, regexPattern, hint,0,null);
                dslSupport.registerInterest(blackListSupportRegex);
                return null;
            }
        };
    }
}
