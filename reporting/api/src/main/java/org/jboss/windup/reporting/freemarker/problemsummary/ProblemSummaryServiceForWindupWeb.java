package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.category.IssueCategoryModel;

/**
 * Gets information about incidents found during the analysis and provides methods for summarizing and analyzing
 * this data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @author <a href="mailto:dklingen@redhat.com">David Klingenberg</a>
 */
public class ProblemSummaryServiceForWindupWeb
{
    protected GraphContext graphContext;
    protected Set<ProjectModel> projectModels;
    protected Set<String> includeTags;
    protected Set<String> excludeTags;

    protected Map<ProjectModel, Map<RuleSummaryKey, ProblemSummary>> projectSummaryMap = new HashMap<>();
    protected Map<ProjectModel, Map<IssueCategoryModel, List<ProblemSummary>>> resultPerProject = new HashMap<>();

    public ProblemSummaryServiceForWindupWeb(
            GraphContext graphContext,
            Set<ProjectModel> projectModels,
            Set<String> includeTags,
            Set<String> excludeTags
    ) {
        this.graphContext = graphContext;
        this.projectModels = projectModels;
        this.includeTags = includeTags;
        this.excludeTags = excludeTags;
    }

    /**
     * Gets lists of {@link ProblemSummary} objects organized by {@link IssueCategoryModel}.
     */
    public Map<ProjectModel, Map<IssueCategoryModel, List<ProblemSummary>>> getProblemSummaries()
    {
        return getProblemSummaries(false, false);
    }

    /**
     * Gets lists of {@link ProblemSummary} objects organized by {@link IssueCategoryModel}.
     */
    public Map<ProjectModel, Map<IssueCategoryModel, List<ProblemSummary>>> getProblemSummaries(
            boolean strictComparison,
            boolean strictExclude
    ) {
        this.projectSummaryMap = new HashMap<>();
        this.resultPerProject = new HashMap<>();


        loadHints(strictComparison, strictExclude);
        loadClassifications();

        return this.resultPerProject;
    }

    private void loadHints(
            boolean strictComparison,
            boolean strictExclude
    ) {

        InlineHintService hintService = new InlineHintService(graphContext);
        final Iterable<InlineHintModel> hints = projectModels == null ?
                hintService.findAll() :
                hintService.getHintsForProjects(projectModels);

        for (InlineHintModel hint : hints)
        {
            Set<String> tags = hint.getTags();

            boolean hasTagMatch;

            if (strictComparison)
            {
                hasTagMatch = TagUtil.strictCheckMatchingTags(tags, includeTags, excludeTags);
            }
            else
            {
                hasTagMatch = TagUtil.checkMatchingTags(tags, includeTags, excludeTags, strictExclude);
            }

            if (!hasTagMatch)
            {
                continue;
            }

            FileModel file = hint.getFile();
            Iterable<ProjectModel> projectModelsIterable = file.getRootProjectModels();

            RuleSummaryKey key = new RuleSummaryKey(hint.getEffort(), hint.getRuleID(), hint.getTitle());

            for (ProjectModel projectModel : projectModelsIterable)
            {
                Map<RuleSummaryKey, ProblemSummary> ruleSummaryMap = projectSummaryMap.computeIfAbsent(projectModel, k -> new HashMap<>());

                ProblemSummary summary = ruleSummaryMap.get(key);

                if (summary == null)
                {
                    summary = new ProblemSummary(
                            UUID.randomUUID().toString(),
                            hint.getIssueCategory(),
                            hint.getRuleID(),
                            hint.getTitle(),
                            1,
                            hint.getEffort()
                    );

                    for (LinkModel link : hint.getLinks())
                    {
                        summary.addLink(link.getDescription(), link.getLink());
                    }

                    ruleSummaryMap.put(key, summary);
                    this.addToResult(summary, projectModel);
                }
                else
                {
                    summary.setNumberFound(summary.getNumberFound() + 1);
                }

                summary.addFile(hint.getHint(), file);
            }
        }
    }

    private void loadClassifications() {
        ClassificationService classificationService = new ClassificationService(graphContext);

        for (ClassificationModel classification : classificationService.findAll())
        {
            Set<String> tags = classification.getTags();
            if (!TagUtil.checkMatchingTags(tags, includeTags, excludeTags, false))
                continue;

            List<FileModel> newFileModels = new ArrayList<>();
            for (FileModel file : classification.getFileModels())
            {
                if (projectModels != null)
                {
                    // make sure this one is in the project
                    if (!projectModels.contains(file.getProjectModel()))
                        continue;
                }
                newFileModels.add(file);
            }

            if (newFileModels.isEmpty())
                continue;

            RuleSummaryKey key = new RuleSummaryKey(classification.getEffort(), classification.getRuleID(), classification.getClassification());


            Set<ProjectModel> projectModels = new HashSet<>();
            newFileModels.forEach(fileModel -> fileModel.getRootProjectModels().forEach(projectModels::add));

            for (ProjectModel projectModel : projectModels)
            {
                Map<RuleSummaryKey, ProblemSummary> ruleSummaryMap = this.projectSummaryMap.computeIfAbsent(projectModel, k -> new HashMap<>());
                ProblemSummary summary = ruleSummaryMap.get(key);

                if (summary == null)
                {
                    summary = new ProblemSummary(
                            UUID.randomUUID().toString(),
                            classification.getIssueCategory(),
                            classification.getRuleID(),
                            classification.getClassification(),
                            0,
                            classification.getEffort()
                    );

                    for (LinkModel link : classification.getLinks())
                    {
                        summary.addLink(link.getDescription(), link.getLink());
                    }

                    ruleSummaryMap.put(key, summary);
                    this.addToResult(summary, projectModel);
                }

                for (FileModel file : newFileModels)
                {
                    summary.addFile(classification.getDescription(), file);
                    file.getRootProjectModels();
                }

                summary.setNumberFound(summary.getNumberFound() + newFileModels.size());
            }
        }
    }

    protected void addToResult(ProblemSummary summary, ProjectModel project)
    {
        Map<IssueCategoryModel, List<ProblemSummary>> resultMap = this.resultPerProject.computeIfAbsent(project, k -> new TreeMap<>(new IssueCategoryModel.IssueSummaryPriorityComparator()));
        List<ProblemSummary> problemSummaries = resultMap.computeIfAbsent(summary.getIssueCategoryModel(), k -> new ArrayList<>());
        problemSummaries.add(summary);
    }
}
