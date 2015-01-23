package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleLifecycleListener;

/**
 * Makes sure to clear the {@link TypeInterestFactory} before and after each execution of Windup.
 * 
 * @author jsightler
 *
 */
public class TypeInterestFactoryLifecycleListener extends AbstractRuleLifecycleListener implements RuleLifecycleListener
{
    @Override
    public void beforeExecution(GraphRewrite event)
    {
        TypeInterestFactory.clear();
    }

    @Override
    public void afterExecution(GraphRewrite event)
    {
        TypeInterestFactory.clear();
    }

}
