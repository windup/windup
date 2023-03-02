package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationPackageIncidentsDto;
import org.jboss.windup.rules.apps.java.service.TypeReferenceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class PackageIncidentsRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "packages-incidents";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        GraphContext context = event.getGraphContext();

        List<ApplicationPackageIncidentsDto> result = new ArrayList<>();
        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel projectModel = inputPath.getProjectModel();

            Set<String> includeTags = new HashSet<>();
            Set<String> excludeTags = new HashSet<>();

            TypeReferenceService typeReferenceService = new TypeReferenceService(context);
            Map<String, Integer> packages = typeReferenceService.getPackageUseFrequencies(projectModel, includeTags, excludeTags, 2, true);

            ApplicationPackageIncidentsDto applicationPackageIncidentsDto = new ApplicationPackageIncidentsDto();
            applicationPackageIncidentsDto.setApplicationId(projectModel.getId().toString());
            applicationPackageIncidentsDto.setPackages(packages);
            result.add(applicationPackageIncidentsDto);
        }
        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
