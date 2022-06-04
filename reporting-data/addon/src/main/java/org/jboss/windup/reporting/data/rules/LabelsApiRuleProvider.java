package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.loader.LabelLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.LabelDto;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class LabelsApiRuleProvider extends AbstractApiRuleProvider {

    @Inject
    private LabelLoader labelLoader;

    @Override
    public String getOutputFilename() {
        return "labels.json";
    }

    @Override
    public Object getData(GraphRewrite event) {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
        List<Path> userLabelPaths = cfg.getUserLabelsPaths().stream()
                .map(fileModel -> fileModel.asFile().toPath())
                .collect(Collectors.toList());

        List<Label> labels = new ArrayList<>();
        RuleLoaderContext labelLoaderContext = new RuleLoaderContext(userLabelPaths, null);
        LabelProviderRegistry labelProviderRegistry = labelLoader.loadConfiguration(labelLoaderContext);
        for (LabelProvider provider : labelProviderRegistry.getProviders()) {
            labels.addAll(provider.getData().getLabels());
        }

        return labels.stream().map(label -> {
            LabelDto labelDto = new LabelDto();

            labelDto.id = label.getId();
            labelDto.name = label.getName();
            labelDto.description = label.getDescription();
            labelDto.supported = new HashSet<>(label.getSupported());
            labelDto.unsuitable = new HashSet<>(label.getUnsuitable());
            labelDto.neutral = new HashSet<>(label.getNeutral());

            return labelDto;
        }).collect(Collectors.toList());
    }
}
