package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link InitialAnalysisPhase}<br/>
 * Next: {@link PostMigrationRulesPhase}
 * 
 * <p>
 * Most {@link Rule}s will go in this {@link RulePhase}. These include {@link Rule}s that detect code in the source application (or server) that will
 * need to be changed and produce metadata to be reported on regarding these changes.
 * </p>
 * 
 * @author jsightler
 *
 */
public class MigrationRulesPhase extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(InitialAnalysisPhase.class);
    }
}
