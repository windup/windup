package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers Java-style .properties files and places them into the graph.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverPropertiesFilesRuleProvider extends IteratingRuleProvider<FileModel>
{
    private static final String TECH_TAG = "Properties";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    public DiscoverPropertiesFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverPropertiesFilesRuleProvider.class)
                    .setPhase(ClassifyFileTypesPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Properties Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(FileModel.class).withProperty(FileModel.IS_DIRECTORY, false)
                    .withProperty(FileModel.FILE_PATH, QueryPropertyComparisonType.REGEX,
                                ".*\\.properties$");
    }

    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        ExecutionStatistics.get().begin("DiscoverPropertiesFilesRuleProvider.perform");
        GraphService<PropertiesModel> service = new GraphService<>(event.getGraphContext(), PropertiesModel.class);
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        PropertiesModel properties = service.addTypeToModel(payload);
        properties.setGenerateSourceReport(true);

        GraphService.addTypeToModel(event.getGraphContext(), payload, SourceFileModel.class);

        technologyTagService.addTagToFileModel(payload, TECH_TAG, TECH_TAG_LEVEL);

        ExecutionStatistics.get().end("DiscoverPropertiesFilesRuleProvider.perform");
    }
}
