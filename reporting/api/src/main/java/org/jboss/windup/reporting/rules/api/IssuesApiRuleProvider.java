package org.jboss.windup.reporting.rules.api;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummary;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummaryService;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.reporting.service.SourceReportService;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class IssuesApiRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getOutputFilename() {
        return "issues.json";
    }

    @Override
    public Object getData(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        Set<String> includeTags = Collections.emptySet();
        Set<String> excludeTags = Collections.emptySet();

        SourceReportService sourceReportService = new SourceReportService(context);

        return new ProjectService(context).getRootProjectModels().stream().map(projectModel -> {
            Set<ProjectModel> projectModels = getProjects(projectModel);

            Map<IssueCategoryModel, List<ProblemSummary>> problemSummariesOriginal = ProblemSummaryService
                    .getProblemSummaries(event.getGraphContext(), projectModels, includeTags, excludeTags);

            // Convert the keys to String to make Freemarker happy
            Comparator<IssueCategoryModel> severityComparator = new IssueCategoryModel.IssueSummaryPriorityComparator();
            Map<IssueCategoryModel, List<ProblemSummary>> problemSummaries = new TreeMap<>(severityComparator);
            problemSummaries.putAll(problemSummariesOriginal);

            Map<String, List<ProblemSummary>> primarySummariesByString = new LinkedHashMap<>(problemSummariesOriginal.size());
            for (Map.Entry<IssueCategoryModel, List<ProblemSummary>> entry : problemSummaries.entrySet()) {
                String severityString = entry.getKey() == null ? null : entry.getKey().getName();
                primarySummariesByString.put(severityString, entry.getValue());
            }

            // Fill Data
            ApplicationIssuesData applicationIssuesData = new ApplicationIssuesData();
            applicationIssuesData.applicationId = projectModel.getId().toString();
            applicationIssuesData.issues = new HashMap<>();

            for (Map.Entry<String, List<ProblemSummary>> entry : primarySummariesByString.entrySet()) {
                String key = entry.getKey();
                List<IssueData> value = entry.getValue().stream().map(problemSummary -> {
                    IssueData issueData = new IssueData();

                    issueData.id = problemSummary.getId().toString();
                    issueData.ruleId = problemSummary.getRuleID();
                    issueData.levelOfEffort = EffortReportService.getEffortLevelDescription(EffortReportService.Verbosity.VERBOSE, problemSummary.getEffortPerIncident());
                    issueData.name = problemSummary.getIssueName();
                    issueData.links = problemSummary.getLinks().stream().map(link -> {
                        IssueLinkData issueLinkData = new IssueLinkData();
                        issueLinkData.title = link.getTitle();
                        issueLinkData.href = link.getLink();
                        return issueLinkData;
                    }).collect(Collectors.toList());
                    issueData.affectedFiles = StreamSupport.stream(problemSummary.getDescriptions().spliterator(), false)
                            .map(description -> {
                                IssueAffectedFilesData issueAffectedFilesData = new IssueAffectedFilesData();
                                issueAffectedFilesData.description = description;
                                issueAffectedFilesData.files = StreamSupport.stream(problemSummary.getFilesForDescription(description).spliterator(), false).map(fileSummary -> {
                                    IssueFileData issueFileData = new IssueFileData();
                                    issueFileData.id = fileSummary.getFile().getId().toString();
                                    issueFileData.fileName = fileSummary.getFile().getPrettyPath();
                                    issueFileData.occurrences = fileSummary.getOccurrences();
                                    return issueFileData;
                                }).collect(Collectors.toList());
                                return issueAffectedFilesData;
                            })
                            .collect(Collectors.toList());

                    return issueData;
                }).collect(Collectors.toList());

                applicationIssuesData.issues.put(key.toLowerCase().replaceAll("migration ", ""), value);
            }

            return applicationIssuesData;
        }).collect(Collectors.toList());
    }

    private Set<ProjectModel> getProjects(ProjectModel projectModel) {
        if (projectModel == null) {
            return null;
        }

        ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy());
        return traversal.getAllProjects(true);
    }

    static class ApplicationIssuesData {
        public String applicationId;
        public Map<String, List<IssueData>> issues;
    }

    static class IssueData {
        public String id;
        public String name;
        public String ruleId;
        public String levelOfEffort;
        public List<IssueLinkData> links;
        public List<IssueAffectedFilesData> affectedFiles;
    }

    static class IssueAffectedFilesData {
        public String description;
        public List<IssueFileData> files;
    }

    static class IssueFileData {
        public String id;
        public String fileName;
        public int occurrences;
    }

    static class IssueLinkData {
        public String title;
        public String href;
    }
}
