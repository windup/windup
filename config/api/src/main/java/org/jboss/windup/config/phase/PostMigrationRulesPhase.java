package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link MigrationRulesPhase}<br/>
 * Next: {@link PreReportGenerationPhase}
 * 
 * <p>
 * This occurs immediately after {@link MigrationRulesPhase}. This can be used in cases where some rule wants to execute immediately after all other
 * migration rules. The primary use case at the moment involves unit tests.
 * </p>
 * 
 * @author jsightler
 *
 */
public class PostMigrationRulesPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(MigrationRulesPhase.class);
    }
}
