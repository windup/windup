package org.jboss.windup.rules.apps.javaee.rules;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Discovers Hibernate Configuration Files (eg, hibernate.cfg.xml), extracts their metadata, and places this metadata
 * into the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class DiscoverHibernateConfigurationRuleProvider extends IteratingRuleProvider<DoctypeMetaModel>
{
    private static final String TECH_TAG = "Hibernate Cfg";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    private static final String hibernateRegex = "(?i).*hibernate.configuration.*";

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverXmlFilesRuleProvider.class);
    }

    @Override
    public ConditionBuilder when()
    {
        QueryGremlinCriterion doctypeSearchCriterion = new QueryGremlinCriterion()
        {
            @Override
            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.has(DoctypeMetaModel.PROPERTY_PUBLIC_ID, Text.REGEX, hibernateRegex);

                FramedGraphQuery systemIDQuery = event.getGraphContext().getQuery().type(DoctypeMetaModel.class)
                            .has(DoctypeMetaModel.PROPERTY_SYSTEM_ID, Text.REGEX, hibernateRegex);
                GremlinPipeline<Vertex, Vertex> systemIdPipeline = new GremlinPipeline<>(systemIDQuery.vertices());

                pipeline.add(systemIdPipeline);

                pipeline.dedup();
            }
        };

        return Query.find(DoctypeMetaModel.class).piped(doctypeSearchCriterion);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, DoctypeMetaModel payload)
    {
        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(
                    event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        String publicId = payload.getPublicId();
        String systemId = payload.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = extractVersion(publicId, systemId);

        for (XmlFileModel xml : payload.getXmlResources())
        {
            // check the root XML node.
            HibernateConfigurationFileModel hibernateCfgModel = hibernateConfigurationFileService
                        .addTypeToModel(xml);
            technologyTagService.addTagToFileModel(hibernateCfgModel, TECH_TAG, TECH_TAG_LEVEL);

            if (StringUtils.isNotBlank(versionInformation))
            {
                hibernateCfgModel.setSpecificationVersion(versionInformation);
            }
        }
    }

    private String extractVersion(String publicId, String systemId)
    {
        Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z.-]+");

        if (StringUtils.isNotBlank(publicId))
        {
            Matcher matcher = pattern.matcher(publicId);
            if (matcher.find())
            {
                return matcher.group();
            }
        }

        if (StringUtils.isNotBlank(systemId))
        {
            Matcher matcher = pattern.matcher(systemId);
            if (matcher.find())
            {
                String match = matcher.group();

                // for system ID, make sure to remove the ".dtd" that could come in.
                return StringUtils.removeEnd(match, ".dtd");
            }
        }

        return null;
    }
}
