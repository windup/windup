package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers Java-style .properties files and places them into the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class DiscoverPropertiesFilesRuleProvider extends IteratingRuleProvider<FileModel>
{
    private static final String TECH_TAG = "Properties";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.find(FileModel.class).withProperty(FileModel.FILE_NAME, QueryPropertyComparisonType.REGEX,
                    ".*.properties$");
    }

    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        GraphService<PropertiesModel> service = new GraphService<>(event.getGraphContext(), PropertiesModel.class);
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        PropertiesModel properties = service.create();
        properties.setFileResource(payload);

        GraphService.addTypeToModel(event.getGraphContext(), payload, SourceFileModel.class);

        technologyTagService.addTagToFileModel(payload, TECH_TAG, TECH_TAG_LEVEL);

        try (InputStream is = payload.asInputStream())
        {
            Properties props = new Properties();
            props.load(is);

            for (Object key : props.keySet())
            {
                String property = StringUtils.trim(key.toString());
                String propertyValue = StringUtils.trim(props.get(key).toString());
                properties.setProperty(property, propertyValue);
            }
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to load properties file: " + payload.getFilePath() + " due to: "
                        + e.getMessage());
        }
    }
}
