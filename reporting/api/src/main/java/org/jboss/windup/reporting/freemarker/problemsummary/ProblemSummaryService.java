package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.category.IssueCategoryModel;

/**
 * Gets information about incidents found during the analysis and provides methods for summarizing and analyzing
 * this data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class ProblemSummaryService {
    /**
     * Gets lists of {@link ProblemSummary} objects organized by {@link IssueCategoryModel}.
     */
    public static Map<IssueCategoryModel, List<ProblemSummary>> getProblemSummaries(
            GraphContext graphContext,
            Set<ProjectModel> projectModels,
            Set<String> includeTags,
            Set<String> excludeTags) {
        return getProblemSummaries(graphContext, projectModels, includeTags, excludeTags, false, false);
    }

    /**
     * Gets lists of {@link ProblemSummary} objects organized by {@link IssueCategoryModel}.
     */
    public static Map<IssueCategoryModel, List<ProblemSummary>> getProblemSummaries(GraphContext graphContext, Set<ProjectModel> projectModels, Set<String> includeTags,
                                                                                    Set<String> excludeTags,
                                                                                    boolean strictComparison,
                                                                                    boolean strictExclude) {
        // The key is the severity as a String
        Map<IssueCategoryModel, List<ProblemSummary>> results = new TreeMap<>(new IssueCategoryModel.IssueSummaryPriorityComparator());
        Map<RuleSummaryKey, ProblemSummary> ruleToSummary = new HashMap<>();

        InlineHintService hintService = new InlineHintService(graphContext);
        final Iterable<InlineHintModel> hints = projectModels == null ? hintService.findAll() : hintService.getHintsForProjects(new ArrayList<>(projectModels));
        for (InlineHintModel hint : hints) {
            if (hint.getIssueDisplayMode() == IssueDisplayMode.DETAIL_ONLY)
                continue;

            Set<String> tags = hint.getTags();

            boolean hasTagMatch;

            if (strictComparison) {
                hasTagMatch = TagUtil.strictCheckMatchingTags(tags, includeTags, excludeTags);
            } else {
                hasTagMatch = TagUtil.checkMatchingTags(tags, includeTags, excludeTags, strictExclude);
            }

            if (!hasTagMatch) {
                continue;
            }

            RuleSummaryKey key = new RuleSummaryKey(hint.getEffort(), hint.getRuleID(), hint.getTitle());

            ProblemSummary summary = ruleToSummary.get(key);
            if (summary == null) {
                List<String> sourceTechnologies = hint.getSourceTechnologies().stream().map(TechnologyReference::new).map(TechnologyReference::toString).collect(Collectors.toList());
                List<String> targetTechnologies = hint.getTargetTechnologies().stream().map(TechnologyReference::new).map(TechnologyReference::toString).collect(Collectors.toList());
                summary = new ProblemSummary(UUID.randomUUID().toString(), hint.getIssueCategory(), hint.getRuleID(), hint.getTitle(), 1, hint.getEffort(), sourceTechnologies, targetTechnologies);
                for (LinkModel link : hint.getLinks()) {
                    summary.addLink(link.getDescription(), link.getLink());
                }
                ruleToSummary.put(key, summary);
                addToResults(results, summary);
            } else {
                summary.setNumberFound(summary.getNumberFound() + 1);
            }
            summary.addFile(hint.getHint(), hint.getFile());
        }

        ClassificationService classificationService = new ClassificationService(graphContext);
        for (ClassificationModel classification : classificationService.findAll()) {
            if (classification.getIssueDisplayMode() == IssueDisplayMode.DETAIL_ONLY)
                continue;

            Set<String> tags = classification.getTags();
            if (!TagUtil.checkMatchingTags(tags, includeTags, excludeTags, false))
                continue;

            List<FileModel> newFileModels = new ArrayList<>();
            for (FileModel file : classification.getFileModels()) {
                if (projectModels != null) {
                    // make sure this one is in the project
                    if (!projectModels.contains(file.getProjectModel()))
                        continue;
                }
                newFileModels.add(file);
            }

            if (newFileModels.isEmpty())
                continue;

            RuleSummaryKey key = new RuleSummaryKey(classification.getEffort(), classification.getRuleID(), classification.getClassification());
            ProblemSummary summary = ruleToSummary.get(key);
            if (summary == null) {
                summary = new ProblemSummary(UUID.randomUUID().toString(), classification.getIssueCategory(), classification.getRuleID(),
                        classification.getClassification(),
                        0, classification.getEffort(), List.of(), List.of());
                for (LinkModel link : classification.getLinks()) {
                    summary.addLink(link.getDescription(), link.getLink());
                }
                ruleToSummary.put(key, summary);
                addToResults(results, summary);
            }

            for (FileModel file : newFileModels)
                summary.addFile(classification.getDescription(), file);

            summary.setNumberFound(summary.getNumberFound() + newFileModels.size());
        }

        return results;
    }

    private static void addToResults(Map<IssueCategoryModel, List<ProblemSummary>> results, ProblemSummary summary) {
        List<ProblemSummary> list = results.get(summary.getIssueCategoryModel());
        if (list == null) {
            list = new ArrayList<>();
            results.put(summary.getIssueCategoryModel(), list);
        }
        list.add(summary);
    }
}
