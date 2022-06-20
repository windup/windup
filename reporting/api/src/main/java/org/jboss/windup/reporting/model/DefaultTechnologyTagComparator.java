package org.jboss.windup.reporting.model;

import java.util.Comparator;

/**
 * Provides a default sort order for TechnologyTags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DefaultTechnologyTagComparator implements Comparator<TechnologyTagModel> {
    @Override
    public int compare(TechnologyTagModel o1, TechnologyTagModel o2) {
        TechnologyTagLevel level1 = o1.getLevel() != null ? o1.getLevel() : TechnologyTagLevel.INFORMATIONAL;
        TechnologyTagLevel level2 = o2.getLevel() != null ? o2.getLevel() : TechnologyTagLevel.INFORMATIONAL;

        int diff = level1.ordinal() - level2.ordinal();
        if (diff == 0) {
            diff = o1.getName().compareTo(o2.getName());
        }
        return diff;
    }
}
