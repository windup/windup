package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link Decompilation}<br/>
 * Next: {@link MigrationRules}
 * 
 * <p>
 * This phase occurs after the application has been unzipped, files have been discovered (including basic filetype information), and the project
 * structure has been ascertained. {@link Rule}s from this phase will perform tasks such as the analysis of source code for placement within the graph
 * (for use by later {@link Rule}s).
 * </p>
 * 
 * @author jsightler
 *
 */
public class InitialAnalysis extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(Decompilation.class);
    }
}
