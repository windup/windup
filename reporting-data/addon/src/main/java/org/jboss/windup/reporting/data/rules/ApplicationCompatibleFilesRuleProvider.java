package org.jboss.windup.reporting.data.rules;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.graph.traversal.AllTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.data.dto.ApplicationCompatibleFilesDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.query.FindFilesNotClassifiedOrHintedGremlinCriterion;
import org.jboss.windup.rules.apps.java.reporting.rules.EnableCompatibleFilesReportOption;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class ApplicationCompatibleFilesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "compatible-files";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        Boolean generateReport = (Boolean) event.getGraphContext().getOptionMap().get(EnableCompatibleFilesReportOption.NAME);
        if (generateReport == null || !generateReport) {
            return Collections.emptyList();
        }

        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);

        List<ApplicationCompatibleFilesDto> result = new ArrayList<>();
        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationCompatibleFilesDto applicationCompatibleFilesDto = new ApplicationCompatibleFilesDto();
            applicationCompatibleFilesDto.setApplicationId(application.getId().toString());
            applicationCompatibleFilesDto.setArtifacts(new ArrayList<>());

            ProjectModelTraversal traversal = new ProjectModelTraversal(application, new AllTraversalStrategy());
            populateArrayWithArtifactDtos(context, traversal, applicationCompatibleFilesDto.getArtifacts());

            result.add(applicationCompatibleFilesDto);
        }
        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    public void populateArrayWithArtifactDtos(GraphContext context, ProjectModelTraversal traversal, List<ApplicationCompatibleFilesDto.ArtifactDto> accumulator) {
        SourceReportService sourceReportService = new SourceReportService(context);

        ProjectModel projectModel = traversal.getCanonicalProject();
        FileModel rootFileModel = projectModel.getRootFileModel();

        // Dto
        ApplicationCompatibleFilesDto.ArtifactDto artifactDto = new ApplicationCompatibleFilesDto.ArtifactDto();
        artifactDto.setName(rootFileModel.getPrettyPath());
        artifactDto.setFiles(getAllCompatibleFiles(context, traversal)
                .stream()
                .filter(fileModel -> fileModel.getPrettyPathWithinProject() != null && !fileModel.getPrettyPathWithinProject().isEmpty())
                .map(fileModel -> {
                    ApplicationCompatibleFilesDto.FileDto fileDto = new ApplicationCompatibleFilesDto.FileDto();
                    fileDto.setFileName(getFilename(fileModel));
                    fileDto.setFileId(DataUtils.getSourceFileId(sourceReportService, fileModel));

                    return fileDto;
                })
                .collect(Collectors.toList())
        );

        accumulator.add(artifactDto);

        // Children
        ProjectModelTraversal traversal1 = new ProjectModelTraversal(projectModel, new AllTraversalStrategy());
        for (ProjectModelTraversal child : traversal1.getChildren()) {
            populateArrayWithArtifactDtos(context, child, accumulator);
        }
    }

    public List<FileModel> getAllCompatibleFiles(GraphContext context, ProjectModelTraversal traversal) {
        ProjectModel projectModel = traversal.getCanonicalProject();
        List<FileModel> fileModels = projectModel.getFileModelsNoDirectories();

        FindFilesNotClassifiedOrHintedGremlinCriterion criterion = new FindFilesNotClassifiedOrHintedGremlinCriterion();
        List<Vertex> initialFileModelsAsVertices = new ArrayList<>();
        fileModels.forEach(fm -> initialFileModelsAsVertices.add(fm.getElement()));
        Iterable<Vertex> vertices = criterion.query(context, initialFileModelsAsVertices);

        List<FileModel> compatibleFiles = new ArrayList<>();
        for (Vertex v : vertices) {
            FileModel f = context.getFramed().frameElement(v, FileModel.class);

            //we don't want to show our decompiled classes in the report
            boolean wasNotGenerated = f.isWindupGenerated() == null || !f.isWindupGenerated();
            boolean isOfInterestingType = f instanceof JavaSourceFileModel || f instanceof XmlFileModel || f instanceof JavaClassFileModel;
            //we don't want to list .class files that have their decompiled .java file with hints/classifications
            boolean withoutHiddenHints = true;

            if (f instanceof JavaClassFileModel) {
                Iterator<Vertex> decompiled = v.vertices(Direction.OUT, JavaClassFileModel.DECOMPILED_FILE);
                if (decompiled.hasNext()) {
                    withoutHiddenHints = !decompiled.next().vertices(Direction.IN, FileReferenceModel.FILE_MODEL).hasNext();
                }
            }

            if (wasNotGenerated && withoutHiddenHints && isOfInterestingType) {
                //if it passed all the checks, add it
                compatibleFiles.add(f);
            }
        }
        return compatibleFiles;
    }

    public String getFilename(FileModel fileModel) {
        if (fileModel instanceof JavaClassFileModel) {
            return getPath((JavaClassFileModel) fileModel);
        } else if (fileModel instanceof JavaSourceFileModel) {
            return getPath((JavaSourceFileModel) fileModel);
        } else if (fileModel instanceof ReportResourceFileModel) {
            return getPath((ReportResourceFileModel) fileModel);
        } else {
            return fileModel.getPrettyPathWithinProject();
        }
    }

    public String getPath(JavaClassFileModel jcfm) {
        String filename = jcfm.getFileName();
        String packageName = jcfm.getPackageName() == null ? "" : jcfm.getPackageName().replaceAll("\\.", File.separator);
        String qualifiedName = packageName + File.separator + filename;
        String reportFileName = qualifiedName;
        return reportFileName;
    }

    public String getPath(JavaSourceFileModel javaSourceModel) {
        String filename = javaSourceModel.getFileName();
        String packageName = javaSourceModel.getPackageName() == null ? "" : javaSourceModel.getPackageName().replaceAll("\\.", File.separator);
        String qualifiedName = packageName + File.separator + filename;
        String reportFileName = qualifiedName;
        return reportFileName;
    }

    public String getPath(ReportResourceFileModel model) {
        return "resources/" + model.getPrettyPath();
    }

}
