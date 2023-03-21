package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.exec.configuration.options.ExportCSVOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.data.dto.AnalysisConfigurationDto;

import java.util.Collections;
import java.util.Map;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
        haltOnException = true
)
public class AnalysisConfigurationRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "analysis-configuration";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        Boolean exportCsv = (Boolean) context.getOptionMap().get(ExportCSVOption.NAME);

        AnalysisConfigurationDto analysisConfigurationDto = new AnalysisConfigurationDto();
        analysisConfigurationDto.setExportCSV(exportCsv);

        return analysisConfigurationDto;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
