package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.rules.apps.javaee.service.HibernateMappingFileService;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class DiscoverHibernateMappingRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = Logger.getLogger(DiscoverHibernateMappingRuleProvider.class.getSimpleName());

    private static final String hibernateRegex = "(?i).*hibernate.mapping.*";

    @Inject
    private JavaClassService javaClassService;
    @Inject
    private HibernateMappingFileService hibernateMappingFileService;
    @Inject
    private HibernateEntityService hibernateEntityService;
    @Inject
    private XmlFileService xmlFileService;

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
    public Configuration getConfiguration(GraphContext context)
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

        ConditionBuilder hibernateConfigurationsFound = Query.find(DoctypeMetaModel.class)
                    .piped(doctypeSearchCriterion);

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(hibernateConfigurationsFound)
                    .perform(new AddHibernateMappingMetadata());
    }

    private class AddHibernateMappingMetadata extends AbstractIterationOperation<DoctypeMetaModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, DoctypeMetaModel payload)
        {
            String publicId = payload.getPublicId();
            String systemId = payload.getSystemId();

            // extract the version information from the public / system ID.
            String versionInformation = extractVersion(publicId, systemId);

            for (XmlFileModel xml : payload.getXmlResources())
            {
                // create a facet, and then identify the XML.
                HibernateMappingFileModel hibernateMapping = hibernateMappingFileService.addTypeToModel(xml);

                Document doc = xmlFileService.loadDocumentQuiet(hibernateMapping);

                if (!XmlUtil.xpathExists(doc, "/hibernate-mapping", null))
                {
                    LOG.log(Level.INFO, "Docment does not contain Hibernate Mapping.");
                    continue;
                }

                String clzPkg = $(doc).xpath("/hibernate-mapping").attr("package");
                String clzName = $(doc).xpath("/hibernate-mapping/class").attr("name");
                String tableName = $(doc).xpath("/hibernate-mapping/class").attr("table");
                String schemaName = $(doc).xpath("/hibernate-mapping/class").attr("schema");
                String catalogName = $(doc).xpath("/hibernate-mapping/class").attr("catalog");

                if (StringUtils.isBlank(clzName))
                {
                    LOG.log(Level.FINE, "Docment does not contain class name. Skipping.");
                    continue;
                }

                // prepend with package name.
                if (StringUtils.isNotBlank(clzPkg) && !StringUtils.startsWith(clzName, clzPkg))
                {
                    clzName = clzPkg + "." + clzName;
                }

                // get a reference to the Java class.
                JavaClassModel clz = javaClassService.getOrCreate(clzName);

                // create the hibernate facet.
                HibernateEntityModel hibernateEntity = hibernateEntityService.create();
                hibernateEntity.setSpecificationVersion(versionInformation);
                hibernateEntity.setJavaClass(clz);
                hibernateEntity.setTableName(tableName);
                hibernateEntity.setSchemaName(schemaName);
                hibernateEntity.setCatalogName(catalogName);

                // map the entity back to the XML mapping.
                hibernateMapping.addHibernateEntity(hibernateEntity);

                if (StringUtils.isNotBlank(versionInformation))
                {
                    hibernateEntity.setSpecificationVersion(versionInformation);
                    hibernateMapping.setSpecificationVersion(versionInformation);
                }
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
