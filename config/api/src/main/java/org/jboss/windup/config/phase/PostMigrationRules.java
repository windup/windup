package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * This occurs immediately after {@link MigrationRules}.
 * 
 * @author jsightler
 *
 */
public class PostMigrationRules extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(MigrationRules.class);
    }
}
