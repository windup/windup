package org.jboss.windup.exec.rulefilters;

import org.jboss.forge.furnace.versions.MultipleVersionRange;
import org.jboss.forge.furnace.versions.VersionRange;

/**
 * Contains methods for comparing {@link VersionRange} objects in ways that are required by the
 * {@link SourceAndTargetPredicate}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class VersionRangeUtil
{
    /**
     * Takes the given {@link VersionRange} objects and returns true if there is any overlap between the two
     * ranges.
     */
    public static boolean versionRangesOverlap(VersionRange range1, VersionRange range2)
    {
        /*
         * FIXME HACK - The code in MultipleVersionRange works pretty well for this calculation, so we are reusing that.
         *
         * Some of the other range intersection algorithms have design flaws that make them return results incorrectly.
         *
         * This hack needs an extensive unit test to insure that it retains the behavior that we expect.
         */
        MultipleVersionRange range1Multiple;
        if (range1 instanceof MultipleVersionRange)
            range1Multiple = (MultipleVersionRange)range1;
        else
            range1Multiple = new MultipleVersionRange(range1);

        try
        {
            VersionRange intersection = range1Multiple.getIntersection(range2);
            return intersection != null && !intersection.isEmpty();
        } catch (Throwable t)
        {
            // This generally only occurs if there was no intersection
            return false;
        }
    }
}
