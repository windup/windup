package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link Finalize}
 * 
 * <p>
 * This occurs immediately after finalize. This is an ideal place to put {@link Rule}s that would like to be the absolute last things to fire.
 * Examples:
 * 
 * <ul>
 * <li>Reporting on the execution time of previous rules</li>
 * <li>Reporting on all of the rules that have executed and which {@link WindupRuleProvider}s executed them</li>
 * </ul>
 * </p>
 * 
 * @author jsightler
 *
 */
public class PostFinalize extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(Finalize.class);
    }
}
