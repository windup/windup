package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link DiscoveryPhase}<br/>
 * Next: {@link ArchiveMetadataExtractionPhase}
 * 
 * <p>
 * Unzipping of any input files (such as an incoming ear file) occur during this phase.
 * </p>
 * 
 * @author jsightler
 *
 */
public class ArchiveExtractionPhase extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoveryPhase.class);
    }
}
