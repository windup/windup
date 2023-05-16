package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationJBPMsDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.Jbpm3ProcessModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
        haltOnException = true
)
public class ApplicationJBPMRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "jbpm";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        SourceReportService sourceReportService = new SourceReportService(context);
        GraphService<Jbpm3ProcessModel> jbpmProcessService = new GraphService<>(context, Jbpm3ProcessModel.class);
        JavaClassService javaClassService = new JavaClassService(context);

        List<ApplicationJBPMsDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationJBPMsDto applicationJBPMsDto = new ApplicationJBPMsDto();
            applicationJBPMsDto.setApplicationId(application.getId().toString());

            // JBPMs
            List<Jbpm3ProcessModel> jbpm3ProcessModels = jbpmProcessService.findAll().stream()
                    .filter(processModel -> {
                        Set<ProjectModel> applicationsContainingFile = ProjectTraversalCache.getApplicationsForProject(context, processModel.getProjectModel());
                        return applicationsContainingFile.contains(application);
                    })
                    .collect(Collectors.toList());

            WindupVertexListModel<Jbpm3ProcessModel> jbpm3ProcessModelWindupVertexListModel = new GraphService<>(context, WindupVertexListModel.class).create();
            jbpm3ProcessModelWindupVertexListModel.addAll(jbpm3ProcessModels);

            applicationJBPMsDto.setJbpms(StreamSupport.stream(jbpm3ProcessModelWindupVertexListModel.spliterator(), false)
                    .map(jbpm3ProcessModel -> {
                        ApplicationJBPMsDto.JBPMDto jbpmDto = new ApplicationJBPMsDto.JBPMDto();

                        jbpmDto.setFileName(jbpm3ProcessModel.getFileName());
                        jbpmDto.setFileId(DataUtils.getSourceFileId(sourceReportService, jbpm3ProcessModel));
                        jbpmDto.setProcessName(jbpm3ProcessModel.getProcessName());
                        jbpmDto.setProcessNoteCount(jbpm3ProcessModel.getNodeCount());
                        jbpmDto.setProcessDecisionCount(jbpm3ProcessModel.getDecisionCount());
                        jbpmDto.setProcessStateCount(jbpm3ProcessModel.getStateCount());
                        jbpmDto.setProcessTaskCount(jbpm3ProcessModel.getTaskCount());
                        jbpmDto.setProcessSubProcessCount(jbpm3ProcessModel.getSubProcessCount());

                        jbpmDto.setActionHandlers(jbpm3ProcessModel.getActionHandlers().stream()
                                .map(javaClassModel -> {
                                    ApplicationJBPMsDto.ActionHandlerDto actionHandlerDto = new ApplicationJBPMsDto.ActionHandlerDto();

                                    actionHandlerDto.setFileName(javaClassModel.getQualifiedName());
                                    actionHandlerDto.setFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, javaClassModel.getQualifiedName()));

                                    return actionHandlerDto;
                                })
                                .collect(Collectors.toList())
                        );
                        jbpmDto.setDecisionHandlers(jbpm3ProcessModel.getDecisionHandlers().stream()
                                .map(javaClassModel -> {
                                    ApplicationJBPMsDto.DecisionHandlerDto actionHandlerDto = new ApplicationJBPMsDto.DecisionHandlerDto();

                                    actionHandlerDto.setFileName(javaClassModel.getQualifiedName());
                                    actionHandlerDto.setFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, javaClassModel.getQualifiedName()));

                                    return actionHandlerDto;
                                })
                                .collect(Collectors.toList())
                        );

                        return jbpmDto;
                    })
                    .collect(Collectors.toList())
            );

            result.add(applicationJBPMsDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
