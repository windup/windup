package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationTransactionsDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.diva.model.DivaContextModel;
import org.jboss.windup.rules.apps.diva.model.DivaEntryMethodModel;
import org.jboss.windup.rules.apps.diva.model.DivaSqlOpModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationTransactionsRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "transactions";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        SourceReportService sourceReportService = new SourceReportService(context);
        JavaClassService javaClassService = new JavaClassService(context);

        GraphService<DivaContextModel> cxtModelService = new GraphService<>(context, DivaContextModel.class);
        List<DivaContextModel> cxts = cxtModelService.findAll();
        if (cxts.isEmpty()) {
            return Collections.emptyList();
        }

        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        WindupVertexListModel<DivaContextModel> windupVertexListModel = listService.create();
        windupVertexListModel.addAll(cxts);

        List<ApplicationTransactionsDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationTransactionsDto applicationTransactionsDto = new ApplicationTransactionsDto();
            result.add(applicationTransactionsDto);

            applicationTransactionsDto.setApplicationId(application.getId().toString());
            applicationTransactionsDto.setTransactions(StreamSupport.stream(windupVertexListModel.spliterator(), false)
                    .map(divaContextModel -> {
                        ApplicationTransactionsDto.TransactionDto transactionDto = divaContextModel.getConstraints().stream()
                                .map(divaConstraintModel -> {
                                    ApplicationTransactionsDto.TransactionDto dto = new ApplicationTransactionsDto.TransactionDto();

                                    if (divaConstraintModel instanceof DivaEntryMethodModel) {
                                        DivaEntryMethodModel divaEntryMethodModel = (DivaEntryMethodModel) divaConstraintModel;

                                        dto.setMethodName(divaEntryMethodModel.getMethodName());

                                        JavaClassModel clz = divaEntryMethodModel.getJavaClass();
                                        if (clz != null) {
                                            dto.setClassName(clz.getQualifiedName());
                                            dto.setClassFileId(StreamSupport.stream(javaClassService.getJavaSource(clz.getQualifiedName()).spliterator(), false)
                                                    .map(sourceReportService::getSourceReportForFileModel)
                                                    .filter(Objects::nonNull)
                                                    .map(f -> f.getSourceFileModel().getId().toString())
                                                    .findFirst()
                                                    .orElse(null)
                                            );
                                        }
                                    }

                                    return dto;
                                })
                                .findFirst()
                                .orElseGet(ApplicationTransactionsDto.TransactionDto::new);

                        transactionDto.setStackTraces(divaContextModel.getTransactions().stream()
                                .flatMap(divaTxModel -> divaTxModel.getOps().stream()
                                        .map(divaOpModel -> {
                                            ApplicationTransactionsDto.StackTraceDto dto = new ApplicationTransactionsDto.StackTraceDto();
                                            if (divaOpModel instanceof DivaSqlOpModel) {
                                                DivaSqlOpModel op = (DivaSqlOpModel) divaOpModel;
                                                dto.setSql(op.getSql());
                                            }

                                            dto.setLineNumber(divaOpModel.getStackTrace().getLocation().getLineNumber());
                                            return dto;
                                        })
                                )
                                .collect(Collectors.toList())
                        );

                        return transactionDto;
                    })
                    .collect(Collectors.toList())
            );
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
