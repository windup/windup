package org.jboss.windup.reporting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.ReportFileModel;

/**
 * This contains utility methods for working with tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TagUtil
{
    /**
     * <p>
     * This method determines whether or not the given tag should be used based upon the given set of include and exclude tags.
     * </p>
     *
     * <p>
     * If the tag is in the exclude tags, it will always return false.
     * </p>
     *
     * <p>
     * If it is not in the exclude tags and the includeTags are empty, it will always return true.
     * </p>
     *
     * <p>
     * If it is not in the exclude tags and it is in the include tags, then it will return true.
     * </p>
     *
     * <p>
     * Otherwise, it will return false.
     * </p>
     */
    public static boolean includeTag(String tag, Set<String> includeTags, Set<String> excludeTags)
    {
        if (excludeTags.contains(tag))
            return false;

        if (includeTags.isEmpty())
            return true;

        return includeTags.contains(tag);
    }

    /**
     * <p>
     * If any tag is in the exclude list, this will return false.
     * </p>
     *
     * <p>
     * If the includeTags list is empty or if any tag is in the includeTags list, return true.
     * </p>
     * <p>
     * Otherwise, return false.
     * </p>
     */
    public static boolean includeTag(Collection<String> tags, Set<String> includeTags, Set<String> excludeTags)
    {
        boolean foundIncludeMatch = false;

        if (tags.isEmpty() && includeTags.isEmpty())
            return true;

        for (String tag : tags)
        {
            if (excludeTags.contains(tag))
                return false;

            if (includeTags.isEmpty() || includeTags.contains(tag))
                foundIncludeMatch = true;
        }
        return foundIncludeMatch;
    }

    public static boolean hasHintsOrClassificationsWithRelevantTags(ReportFileModel reportFileModel, Set<String> includeTags, Set<String> excludeTags)
    {
        Set<String> allTags = new HashSet<>();

        for (ClassificationModel classificationModel : reportFileModel.getClassificationModels())
        {
            for (String tag : classificationModel.getTags())
                allTags.add(tag);
        }

        for (InlineHintModel inlineHintModel : reportFileModel.getInlineHints())
        {
            for (String tag : inlineHintModel.getTags())
                allTags.add(tag);
        }

        return includeTag(allTags, includeTags, excludeTags);
    }
}
