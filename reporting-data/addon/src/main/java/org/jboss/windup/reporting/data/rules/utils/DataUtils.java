package org.jboss.windup.reporting.data.rules.utils;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ReportFileModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class DataUtils {

   public static final Function<Optional<ReportFileModel>, String> FILE_ID_MAPPER = reportFileModel -> reportFileModel
            .map(f -> f.getId().toString())
            .orElse(null);

    public static Optional<ReportFileModel> getSourceFile(
            JavaClassService javaClassService,
            SourceReportService sourceReportService,
            String classQualifiedName
    ) {
        return StreamSupport.stream(javaClassService.getJavaSource(classQualifiedName).spliterator(), false)
                .map(sourceReportService::getSourceReportForFileModel)
                .filter(Objects::nonNull)
                .map(SourceReportModel::getSourceFileModel)
                .findFirst();
    }

    public static String getSourceFileId(
            JavaClassService javaClassService,
            SourceReportService sourceReportService,
            String classQualifiedName
    ) {
        Optional<ReportFileModel> sourceFile = getSourceFile(javaClassService, sourceReportService, classQualifiedName);
        return FILE_ID_MAPPER.apply(sourceFile);
    }

    public static Optional<ReportFileModel> getSourceFile(
            SourceReportService sourceReportService,
            FileModel fileModel
    ) {
        SourceReportModel sourceReportForFileModel = sourceReportService.getSourceReportForFileModel(fileModel);
        if (sourceReportForFileModel != null) {
            return Optional.ofNullable(sourceReportForFileModel.getSourceFileModel());
        } else {
            return Optional.empty();
        }
    }

    public static String getSourceFileId(
            SourceReportService sourceReportService,
            FileModel fileModel
    ) {
        Optional<ReportFileModel> sourceFile = getSourceFile(sourceReportService, fileModel);
        return FILE_ID_MAPPER.apply(sourceFile);
    }

    // Ids needs to have valid characters since they are written in the file disk
    // Windows does not allow the following characters to be part of a filename: \/:*"<>|
    public static String sanitizeFilename(String id) {
        final String sanitizer = "_";
        return id
                .replaceAll("\\\\", sanitizer)
                .replaceAll("/", sanitizer)
                .replaceAll(":", sanitizer)
                .replaceAll("\\*", sanitizer)
                .replaceAll("\"", sanitizer)
                .replaceAll("<", sanitizer)
                .replaceAll(">", sanitizer)
                .replaceAll("\\|", sanitizer);
    }
}
