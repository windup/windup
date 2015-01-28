package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link InitialAnalysis}<br/>
 * Next: {@link PostMigrationRules}
 * 
 * <p>
 * Most {@link Rule}s will go in this {@link RulePhase}. These include {@link Rule}s that detect code in the source application (or server) that will
 * need to be changed and produce metadata to be reported on regarding these changes.
 * </p>
 * 
 * @author jsightler
 *
 */
public class MigrationRules extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(InitialAnalysis.class);
    }
}
