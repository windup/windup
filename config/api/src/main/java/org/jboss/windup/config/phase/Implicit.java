package org.jboss.windup.config.phase;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This phase can occur during any phase of the execution lifecycle. It's exact placement will be defined by the {@link WindupRuleProvider} itself and
 * the values that it returns from {@link WindupRuleProvider#getExecuteAfter()} and {@link WindupRuleProvider#getExecuteBefore()}.
 * 
 * @author jsightler
 *
 */
public class Implicit extends RulePhase
{

}
