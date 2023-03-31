package org.jboss.windup.reporting.data.rules;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.data.Constants;
import org.jboss.windup.reporting.data.dto.ApplicationIssuesDto;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummary;
import org.jboss.windup.reporting.freemarker.problemsummary.ProblemSummaryService;
import org.jboss.windup.reporting.service.EffortReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
        haltOnException = true
)
public class IssuesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "issues";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        Set<String> includeTags = Collections.emptySet();
        Set<String> excludeTags = Collections.emptySet();

        Set<ProjectModel> allProjects = new ProjectService(context).getRootProjectModels();

        // Adding null element to generate the "ALL ISSUES report"
        allProjects.add(null);

        return allProjects.stream().map(projectModel -> {
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
            String applicationId = Optional.ofNullable(projectModel)
                    .map(val -> val.getId().toString())
                    .orElse(Constants.ALL_APPLICATIONS_ID);

            ApplicationIssuesDto applicationIssuesDto = new ApplicationIssuesDto();
            applicationIssuesDto.setApplicationId(applicationId);
            applicationIssuesDto.setIssues(new HashMap<>());

            for (Map.Entry<String, List<ProblemSummary>> entry : primarySummariesByString.entrySet()) {
                String key = entry.getKey();
                List<ApplicationIssuesDto.IssueDto> value = entry.getValue().stream().map(problemSummary -> {
                    ApplicationIssuesDto.IssueDto issueData = new ApplicationIssuesDto.IssueDto();

                    issueData.setId(problemSummary.getId().toString());
                    issueData.setRuleId(problemSummary.getRuleID());

                    issueData.setSourceTechnologies(problemSummary.getSourceTechnologies());
                    issueData.setTargetTechnologies(problemSummary.getTargetTechnologies());

                    EffortReportService.EffortLevel effortLevel = EffortReportService.EffortLevel.forPoints(problemSummary.getEffortPerIncident());
                    issueData.setEffort(new ApplicationIssuesDto.EffortDto());
                    issueData.getEffort().setType(effortLevel.getShortDescription());
                    issueData.getEffort().setDescription(effortLevel.getVerboseDescription());
                    issueData.getEffort().setPoints(effortLevel.getPoints());

                    issueData.setTotalIncidents(problemSummary.getNumberFound());
                    issueData.setTotalStoryPoints(problemSummary.getNumberFound() * problemSummary.getEffortPerIncident());
                    issueData.setName(problemSummary.getIssueName());
                    issueData.setLinks(problemSummary.getLinks().stream().map(link -> {
                                ApplicationIssuesDto.LinkDto linkDto = new ApplicationIssuesDto.LinkDto();
                                linkDto.setTitle(link.getTitle());
                                linkDto.setHref(link.getLink());
                                return linkDto;
                            }).collect(Collectors.toList())
                    );
                    issueData.setAffectedFiles(StreamSupport.stream(problemSummary.getDescriptions().spliterator(), false)
                            .map(description -> {
                                ApplicationIssuesDto.IssueAffectedFilesDto issueAffectedFilesDto = new ApplicationIssuesDto.IssueAffectedFilesDto();
                                issueAffectedFilesDto.setDescription(description);
                                issueAffectedFilesDto.setFiles(StreamSupport.stream(problemSummary.getFilesForDescription(description).spliterator(), false).map(fileSummary -> {
                                            ApplicationIssuesDto.IssueFileDto issueFileDto = new ApplicationIssuesDto.IssueFileDto();
                                            issueFileDto.setFileId(fileSummary.getFile().getId().toString());
                                            issueFileDto.setFileName(getPrettyPathForFile(fileSummary.getFile()));
                                            issueFileDto.setOccurrences(fileSummary.getOccurrences());
                                            return issueFileDto;
                                        }).collect(Collectors.toList())
                                );
                                return issueAffectedFilesDto;
                            })
                            .collect(Collectors.toList())
                    );

                    return issueData;
                }).collect(Collectors.toList());

                String category = key.toLowerCase().trim()
                        .replaceAll("migration ", "")
                        .replaceAll(" ", "-");
                applicationIssuesDto.getIssues().put(category, value);
            }

            return applicationIssuesDto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private Set<ProjectModel> getProjects(ProjectModel projectModel) {
        if (projectModel == null) {
            return null;
        }

        ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy());
        return traversal.getAllProjects(true);
    }

    public static String getPrettyPathForFile(FileModel fileModel) {
        if (fileModel instanceof JavaClassFileModel) {
            JavaClassFileModel jcfm = (JavaClassFileModel) fileModel;
            if (jcfm.getJavaClass() == null) {
                return fileModel.getPrettyPathWithinProject();
            } else {
                return jcfm.getJavaClass().getQualifiedName();
            }
        } else if (fileModel instanceof ReportResourceFileModel) {
            return "resources/" + fileModel.getPrettyPath();
        } else if (fileModel instanceof JavaSourceFileModel) {
            JavaSourceFileModel javaSourceModel = (JavaSourceFileModel) fileModel;
            String filename = StringUtils.removeEndIgnoreCase(fileModel.getFileName(), ".java");
            String packageName = javaSourceModel.getPackageName();
            return packageName == null || packageName.isEmpty() ? filename : packageName + "." + filename;
        }
        // This is used for instance when showing unparsable files in the Issues Report.
        else if (fileModel instanceof ArchiveModel) {
            return fileModel.getPrettyPath();
        } else {
            return fileModel.getPrettyPathWithinProject();
        }
    }
}
