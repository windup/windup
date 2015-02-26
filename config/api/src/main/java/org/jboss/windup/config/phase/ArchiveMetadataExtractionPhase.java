package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ArchiveExtractionPhase}<br />
 * Next: {@link ClassifyFileTypesPhase}
 * 
 * <p>
 * Occurs immediately after archive extraction. This handles things like calculating checksums for archives or determining the specific type of
 * archive (ear vs other types of archives).
 * </p>
 *
 */
public class ArchiveMetadataExtractionPhase extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ArchiveExtractionPhase.class);
    }
}
