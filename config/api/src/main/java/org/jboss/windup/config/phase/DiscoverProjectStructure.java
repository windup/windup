package org.jboss.windup.config.phase;

import java.util.List;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Previous: {@link ClassifyFileTypes}<br/>
 * Next: {@link Decompilation}
 *
 * <p>
 * Discovering of the project structure of the input application will occur during this phase. This will discover which files are in which project
 * (including determining subprojects) and analyze as much metadata as possible for supported project types (for example, Maven projects).
 * </p>
 * 
 * @author jsightler
 *
 */
public class DiscoverProjectStructure extends RulePhase
{

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(ClassifyFileTypes.class);
    }
}
