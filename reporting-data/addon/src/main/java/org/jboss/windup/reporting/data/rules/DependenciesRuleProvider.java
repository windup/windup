package org.jboss.windup.reporting.data.rules;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.data.dto.ApplicationDependenciesDto;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportDependencyGroupModel;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportToArchiveEdgeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class DependenciesRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "dependencies";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        GraphContext context = event.getGraphContext();

        List<ApplicationDependenciesDto> result = new ArrayList<>();
        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel projectModel = inputPath.getProjectModel();

            List<ApplicationDependenciesDto.DependencyDto> dependencies = addAll(context, new ProjectModelTraversal(projectModel), new HashMap<>());

            ApplicationDependenciesDto applicationDependenciesDto = new ApplicationDependenciesDto();
            applicationDependenciesDto.applicationId = projectModel.getId().toString();
            applicationDependenciesDto.dependencies = dependencies;
            result.add(applicationDependenciesDto);
        }
        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private List<ApplicationDependenciesDto.DependencyDto> addAll(
            GraphContext context,
            ProjectModelTraversal projectModelTraversal,
            Map<String, DependencyReportDependencyGroupModel> groupsBySHA1
    ) {
        List<ApplicationDependenciesDto.DependencyDto> result = new ArrayList<>();

        FileModel rootFileModel = projectModelTraversal.getCurrent().getRootFileModel();

        // Don't create a dependency entry for the entire application (root project)
        boolean isRootProject = projectModelTraversal.getCurrent().getParentProject() == null;
        DependencyReportDependencyGroupModel groupModel;
        if (!isRootProject && rootFileModel instanceof ArchiveModel) {
            ArchiveModel archiveModel = (ArchiveModel) rootFileModel;
            ArchiveModel canonicalArchive;
            if (archiveModel instanceof DuplicateArchiveModel) {
                canonicalArchive = ((DuplicateArchiveModel) archiveModel).getCanonicalArchive();
            } else {
                canonicalArchive = archiveModel;
            }

            // 1. Get SHA1
            String sha1 = archiveModel.getSHA1Hash();

            // 2. Get the group model for this sha1
            boolean shouldDtoBeGenerated = false;
            groupModel = groupsBySHA1.get(sha1);
            if (groupModel == null) {
                shouldDtoBeGenerated = true;

                groupModel = context.service(DependencyReportDependencyGroupModel.class).create();
                groupModel.setSHA1(sha1);
                groupModel.setCanonicalProject(canonicalArchive.getProjectModel());
                groupsBySHA1.put(sha1, groupModel);
            }

            // 3. If the group already has this archive, don't do anything
            String path = projectModelTraversal.getFilePath(rootFileModel);
            boolean archiveAlreadyLinked = false;
            for (DependencyReportToArchiveEdgeModel groupEdge : groupModel.getArchives()) {
                if (StringUtils.equals(groupEdge.getFullPath(), path)) {
                    archiveAlreadyLinked = true;
                    break;
                }
            }

            // Don't add projects that have already been added
            if (!archiveAlreadyLinked) {
                DependencyReportToArchiveEdgeModel edge = groupModel.addArchiveModel(archiveModel);
                edge.setFullPath(path);
            }

            // Generate DTO
            if (shouldDtoBeGenerated) {
                ApplicationDependenciesDto.DependencyDto dependencyDto = new ApplicationDependenciesDto.DependencyDto();
                dependencyDto.name = groupModel.getCanonicalProject().getRootFileModel().getFileName();
                dependencyDto.mavenIdentifier = groupModel.getCanonicalProject().getProperty("mavenIdentifier");
                dependencyDto.sha1 = archiveModel.getSHA1Hash();
                dependencyDto.version = canonicalArchive.getProjectModel().getVersion();
                dependencyDto.organization = canonicalArchive.getProjectModel().getOrganization();
                dependencyDto.foundPaths = groupModel.getArchives().stream()
                        .map(DependencyReportToArchiveEdgeModel::getFullPath)
                        .collect(Collectors.toList());
                result.add(dependencyDto);
            }
        }

        for (ProjectModelTraversal child : projectModelTraversal.getChildren()) {
            List<ApplicationDependenciesDto.DependencyDto> childDependencies = addAll(context, child, groupsBySHA1);
            result.addAll(childDependencies);
        }

        return result;
    }
}
