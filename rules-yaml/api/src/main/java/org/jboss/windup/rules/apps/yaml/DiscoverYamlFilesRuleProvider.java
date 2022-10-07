package org.jboss.windup.rules.apps.yaml;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.yaml.model.YamlFileModel;
import org.jboss.windup.rules.files.FileMapping;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.logging.Logger;

/**
 * Detect and save {@link YamlFileModel}s into the graph.
 */
public class DiscoverYamlFilesRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logger.getLogger(DiscoverYamlFilesRuleProvider.class.getName());

    public DiscoverYamlFilesRuleProvider() {
        super(MetadataBuilder.forProvider(DiscoverYamlFilesRuleProvider.class)
                .setPhase(ClassifyFileTypesPhase.class));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule(FileMapping.from(".*\\.yml$").to(YamlFileModel.class))
                .addRule(FileMapping.from(".*\\.yaml$").to(YamlFileModel.class))
                .addRule()
                .when(Query.fromType(YamlFileModel.class))
                .perform(new AbstractIterationOperation<YamlFileModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, YamlFileModel payload) {
                        YamlFileModel yamlFileModel = GraphService.addTypeToModel(event.getGraphContext(), payload, YamlFileModel.class);
                    }

                    @Override
                    public String toString() {
                        return "IndexYamlFilesMetadata";
                    }
                });
    }

}
