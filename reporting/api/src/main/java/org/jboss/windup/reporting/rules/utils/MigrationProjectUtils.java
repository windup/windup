package org.jboss.windup.reporting.rules.utils;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MigrationProjectUtils {

    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    public MigrationProjectUtils(GraphContext context) {
        this.classificationService = new ClassificationService(context);
        this.inlineHintService = new InlineHintService(context);
    }

    public int getMigrationEffortPoints(ProjectModelTraversal projectModelTraversal,
                                        Boolean recursive,
                                        Set<String> includeTags,
                                        Set<String> excludeTags,
                                        Set<String> issueCategoryIDs
    ) {
        // Get values for classification and hints.
        Map<Integer, Integer> classificationEffortDetails = classificationService
                .getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategoryIDs, recursive, false);
        Map<Integer, Integer> hintEffortDetails = inlineHintService
                .getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategoryIDs, recursive, false);

        Map<Integer, Integer> results = sumMaps(classificationEffortDetails, hintEffortDetails);
        return sumPoints(results);
    }

    private Map<Integer, Integer> sumMaps(Map<Integer, Integer> classificationEffortDetails, Map<Integer, Integer> hintEffortDetails) {
        Map<Integer, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
        results.putAll(classificationEffortDetails);
        for (Map.Entry<Integer, Integer> entry : hintEffortDetails.entrySet()) {
            if (!results.containsKey(entry.getKey()))
                results.put(entry.getKey(), entry.getValue());
            else
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
        }
        return results;
    }

    private int sumPoints(Map<Integer, Integer> results) {
        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : results.entrySet()) {
            sum += entry.getKey() * entry.getValue();
        }
        return sum;
    }
}
