package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ArchiveMetadataExtractionPhase}<br/>
 * Next: {@link DiscoverProjectStructurePhase}<br/>
 * 
 * <p>
 * This scans files and attaches metadata to them. For example, this may find all of the Java files in an application and mark them as Java, or it may
 * find all of the bash scripts in an input and identify them appropriately.
 * </p>
 * .
 *
 */
public class ClassifyFileTypesPhase extends RulePhase
{
    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ArchiveMetadataExtractionPhase.class);
    }

}
