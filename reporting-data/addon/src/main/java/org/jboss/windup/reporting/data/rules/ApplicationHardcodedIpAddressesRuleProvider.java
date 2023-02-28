package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationHardcodedIpAddressesDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.ip.HardcodedIPLocationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationHardcodedIpAddressesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "hardcoded-ip-addresses";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        SourceReportService sourceReportService = new SourceReportService(context);
        GraphService<HardcodedIPLocationModel> ipLocationModelService = new GraphService<>(context, HardcodedIPLocationModel.class);

        List<ApplicationHardcodedIpAddressesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationHardcodedIpAddressesDto applicationHardcodedIpAddressesDto = new ApplicationHardcodedIpAddressesDto();
            applicationHardcodedIpAddressesDto.setApplicationId(application.getId().toString());

            // Files
            List<HardcodedIPLocationModel> hardcodedIPLocationModels = ipLocationModelService.findAll().stream()
                    .filter(location -> {
                        Set<ProjectModel> applicationsForFile = ProjectTraversalCache.getApplicationsForProject(context, location.getFile().getProjectModel());
                        return applicationsForFile.contains(application);
                    })
                    .collect(Collectors.toList());

            WindupVertexListModel<HardcodedIPLocationModel> hardcodedIPLocationModelWindupVertexListModel = new GraphService<>(context, WindupVertexListModel.class).create();
            hardcodedIPLocationModelWindupVertexListModel.addAll(hardcodedIPLocationModels);

            applicationHardcodedIpAddressesDto.setFiles(StreamSupport.stream(hardcodedIPLocationModelWindupVertexListModel.spliterator(), false)
                    .map(locationModel -> {
                        ApplicationHardcodedIpAddressesDto.FileDto fileDto = new ApplicationHardcodedIpAddressesDto.FileDto();
                        fileDto.setLineNumber(locationModel.getLineNumber());
                        fileDto.setColumnNumber(locationModel.getColumnNumber());
                        fileDto.setIpAddress(locationModel.getSourceSnippit());
                        fileDto.setFileId(sourceReportService.getSourceReportForFileModel(((FileLocationModel) locationModel).getFile())
                                .getSourceFileModel().getId()
                                .toString()
                        );
                        return fileDto;
                    })
                    .collect(Collectors.toList())
            );

            result.add(applicationHardcodedIpAddressesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
