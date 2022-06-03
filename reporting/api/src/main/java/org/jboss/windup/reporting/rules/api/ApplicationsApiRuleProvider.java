package org.jboss.windup.reporting.rules.api;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.AllTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.TechnologyTagService;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class ApplicationsApiRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getOutputFilename() {
        return "applications.json";
    }

    @Override
    public Object getData(GraphRewrite event) {
        GraphContext context = event.getGraphContext();

        ClassificationService classificationService = new ClassificationService(context);
        InlineHintService inlineHintService = new InlineHintService(context);

        return new ProjectService(context).getRootProjectModels().stream()
                .map(projectModel -> {
                    Data data = new Data();


                    AllTraversalStrategy traversalStrategy = new AllTraversalStrategy();
                    ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(projectModel, traversalStrategy);

                    // Tags
                    Set<String> tags = new HashSet<>();
                    Iterable<TechnologyTagModel> technologyTagModels = new TechnologyTagService(context).findTechnologyTagsForProject(projectModelTraversal);
                    for (TechnologyTagModel tag : technologyTagModels) {
                        if (!Objects.equals(tag.getName(), "Decompiled Java File")) {
                            tags.add(getTagFrom(tag));
                        }
                    }

                    // Story points
                    Set<String> includeTags = Collections.emptySet();
                    Set<String> excludeTags = Collections.emptySet();
                    Set<String> issueCategories = Collections.emptySet();

                    Map<Integer, Integer> classificationEffortDetails = classificationService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategories, true, false);
                    Map<Integer, Integer> hintEffortDetails = inlineHintService.getMigrationEffortByPoints(projectModelTraversal, includeTags, excludeTags, issueCategories, true, false);
                    Map<Integer, Integer> results = sumMaps(classificationEffortDetails, hintEffortDetails);

                    int storyPoints = sumPoints(results);

                    // Incidents
                    Map<IssueCategoryModel, Integer> incidentsClassificationEffortDetails = classificationService.getMigrationEffortBySeverity(event, projectModelTraversal, includeTags, excludeTags, Collections.emptySet(), true);
                    Map<IssueCategoryModel, Integer> incidentsHintEffortDetails = inlineHintService.getMigrationEffortBySeverity(event, projectModelTraversal, includeTags, excludeTags, Collections.emptySet(), true);

                    Map<IssueCategoryModel, Integer> allIncidents = new TreeMap<>(new IssueCategoryModel.IssueSummaryPriorityComparator());
                    addAllIncidents(allIncidents, incidentsClassificationEffortDetails);
                    addAllIncidents(allIncidents, incidentsHintEffortDetails);

                    Map<String, Integer> incidents = new HashMap<>();
                    for (Map.Entry<IssueCategoryModel, Integer> entry : allIncidents.entrySet()) {
                        String key = entry.getKey().getName().trim()
                                .toLowerCase()
                                .replaceAll(" ", "_")
                                .replaceFirst("migration_", "");
                        incidents.put(key, entry.getValue());
                    }

                    // Fill result
                    data.id = projectModel.getName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "");
                    data.name = projectModel.getName();
                    data.tags = tags;
                    data.storyPoints = storyPoints;
                    data.incidents = incidents;

                    return data;
                }).collect(Collectors.toList());
    }

    private String getTagFrom(TechnologyTagModel technologyTagModel) {
        if (technologyTagModel.getVersion() != null) {
            return technologyTagModel.getName() + " " + technologyTagModel.getVersion();
        } else {
            return technologyTagModel.getName();
        }
    }

    private Map<Integer, Integer> sumMaps(Map<Integer, Integer> classificationEffortDetails, Map<Integer, Integer> hintEffortDetails) {
        Map<Integer, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
        results.putAll(classificationEffortDetails);
        for (Map.Entry<Integer, Integer> entry : hintEffortDetails.entrySet()) {
            if (!results.containsKey(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            } else {
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
            }
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

    private void addAllIncidents(Map<IssueCategoryModel, Integer> results, Map<IssueCategoryModel, Integer> effortDetails) {
        for (Map.Entry<IssueCategoryModel, Integer> entry : effortDetails.entrySet()) {
            if (!results.containsKey(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            } else {
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
            }
        }
    }

    static class Data {
        public String id;
        public String name;
        public Set<String> tags;
        public int storyPoints;
        public Map<String, Integer> incidents;
    }
}
