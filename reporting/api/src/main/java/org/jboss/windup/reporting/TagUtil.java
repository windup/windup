package org.jboss.windup.reporting;

import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.ReportFileModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This contains utility methods for working with tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TagUtil {
    /**
     * @see TagUtil#checkMatchingTags(Collection, Set, Set), only with strictExclude = true.
     */
    public static boolean checkMatchingTags(Collection<String> tags, Set<String> includeTags, Set<String> excludeTags) {
        return TagUtil.checkMatchingTags(tags, includeTags, excludeTags, true);
    }

    /**
     * Returns true if
     * - includeTags is not empty and tag is in includeTags
     * - includeTags is empty and tag is not in excludeTags
     *
     * @param tags        Hint tags
     * @param includeTags Include tags
     * @param excludeTags Exclude tags
     * @return has tag match
     */
    public static boolean strictCheckMatchingTags(Collection<String> tags, Set<String> includeTags, Set<String> excludeTags) {
        boolean includeTagsEnabled = !includeTags.isEmpty();

        for (String tag : tags) {
            boolean isIncluded = includeTags.contains(tag);
            boolean isExcluded = excludeTags.contains(tag);

            if ((includeTagsEnabled && isIncluded) || (!includeTagsEnabled && !isExcluded)) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>
     * If any tag is in the exclude list and strictExclude is true, this will return false.
     * <p>
     * If any tag is in the exclude list and strictExclude is false, then it will depend upon whether or not there is a tag in the includeTags list.
     * </p>
     *
     * <p>
     * If the includeTags list is empty or if any tag is in the includeTags list, return true.
     * </p>
     * <p>
     * Otherwise, return false.
     * </p>
     */
    public static boolean checkMatchingTags(Collection<String> tags, Set<String> includeTags, Set<String> excludeTags, boolean strictExclude) {
        boolean foundIncludeMatch = false;

        if (includeTags.isEmpty())
            return true;

        for (String tag : tags) {
            if (excludeTags.contains(tag))
                // If strict, seeing an excluded tag means this set of tags didn't meet the criteria.
                if (strictExclude)
                    return false;
                    // If not strict, only ignore the excluded tags.
                else
                    continue;

            if (includeTags.isEmpty() || includeTags.contains(tag))
                foundIncludeMatch = true;
        }
        return foundIncludeMatch;
    }

    public static boolean hasHintsOrClassificationsWithRelevantTags(ReportFileModel reportFileModel, Set<String> includeTags, Set<String> excludeTags) {
        Set<String> allTags = gatherReportFileTags(reportFileModel);
        return TagUtil.checkMatchingTags(allTags, includeTags, excludeTags, false);
    }

    public static Set<String> gatherReportFileTags(ReportFileModel reportFileModel) {
        Set<String> allTags = new HashSet<>();
        for (ClassificationModel classificationModel : reportFileModel.getClassificationModels()) {
            for (String tag : classificationModel.getTags())
                allTags.add(tag);
        }
        for (InlineHintModel inlineHintModel : reportFileModel.getInlineHints()) {
            for (String tag : inlineHintModel.getTags())
                allTags.add(tag);
        }
        return allTags;
    }
}
