package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.AllTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.graph.traversal.SharedLibsTraversalStrategy;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.data.dto.ApplicationDto;
import org.jboss.windup.reporting.model.TechnologyTagModel;
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
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class ApplicationsRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "applications";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();

        ClassificationService classificationService = new ClassificationService(context);
        InlineHintService inlineHintService = new InlineHintService(context);

        return new ProjectService(context).getRootProjectModels().stream()
                .map(projectModel -> {
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

                    // Story points in shared archives
                    SharedLibsTraversalStrategy sharedLibsTraversalStrategy = new SharedLibsTraversalStrategy();
                    ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel, sharedLibsTraversalStrategy);

                    classificationEffortDetails = classificationService.getMigrationEffortByPoints(traversal, includeTags, excludeTags, issueCategories, true, false);
                    hintEffortDetails = inlineHintService.getMigrationEffortByPoints(traversal, includeTags, excludeTags, issueCategories, true, false);
                    results = sumMaps(classificationEffortDetails, hintEffortDetails);

                    int storyPointsInSharedArchives = sumPoints(results);

                    // Incidents
                    Map<IssueCategoryModel, Integer> incidentsClassificationEffortDetails = classificationService.getMigrationEffortBySeverity(event, projectModelTraversal, includeTags, excludeTags, Collections.emptySet(), true);
                    Map<IssueCategoryModel, Integer> incidentsHintEffortDetails = inlineHintService.getMigrationEffortBySeverity(event, projectModelTraversal, includeTags, excludeTags, Collections.emptySet(), true);

                    Map<IssueCategoryModel, Integer> allIncidents = new TreeMap<>(new IssueCategoryModel.IssueSummaryPriorityComparator());
                    addAllIncidents(allIncidents, incidentsClassificationEffortDetails);
                    addAllIncidents(allIncidents, incidentsHintEffortDetails);

                    Map<String, Integer> incidents = new HashMap<>();
                    for (Map.Entry<IssueCategoryModel, Integer> entry : allIncidents.entrySet()) {
                        String key = entry.getKey().getName().trim().toLowerCase()
                                .replaceAll("migration ", "")
                                .replaceAll(" ", "-");
                        incidents.put(key, entry.getValue());
                    }

                    // Fill result
                    ApplicationDto applicationDto = new ApplicationDto();

                    boolean isProjectVirtual = Objects.equals(projectModel.getProjectType(), "VIRTUAL");

                    String applicationName;
                    if (projectModel.getRootFileModel() instanceof ApplicationModel) {
                        applicationName = ((ApplicationModel) projectModel.getRootFileModel()).getApplicationName();
                    } else {
                        applicationName = projectModel.getName();
                    }

                    applicationDto.setId(projectModel.getId().toString());
                    applicationDto.setName(isProjectVirtual && projectModel.getName() != null ? projectModel.getName() : applicationName);
                    applicationDto.setIsVirtual(isProjectVirtual);
                    applicationDto.setTags(tags);
                    applicationDto.setStoryPoints(storyPoints);
                    applicationDto.setStoryPointsInSharedArchives(!isProjectVirtual ? storyPointsInSharedArchives : 0);
                    applicationDto.setIncidents(incidents);

                    return applicationDto;
                }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private String getTagFrom(TechnologyTagModel technologyTagModel) {
        if (technologyTagModel.getVersion() != null) {
            return technologyTagModel.getName() + " " + technologyTagModel.getVersion();
        } else {
            return technologyTagModel.getName();
        }
    }

    public static Map<Integer, Integer> sumMaps(Map<Integer, Integer> classificationEffortDetails, Map<Integer, Integer> hintEffortDetails) {
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

    public static int sumPoints(Map<Integer, Integer> results) {
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
}
