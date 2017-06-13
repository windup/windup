package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateMappingFileModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;
import org.jboss.windup.rules.apps.javaee.service.HibernateMappingFileService;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.config.metadata.RuleMetadata;


/**
 * Discovers the hibernate.hbm.xml files.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover hibernate.hbm.xml files")
public class DiscoverHibernateMappingRuleProvider extends IteratingRuleProvider<DoctypeMetaModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverHibernateMappingRuleProvider.class.getName());

    private static final String TECH_TAG = "Hibernate Mapping";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    private static final String REGEX_HIBERNATE = "(?i).*hibernate.mapping.*";


    @Override
    public ConditionBuilder when()
    {

        QueryGremlinCriterion doctypeSearchCriterion = new QueryGremlinCriterion()
        {
            @Override
            public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
            {
                pipeline.has(DoctypeMetaModel.PROPERTY_PUBLIC_ID, Text.REGEX, REGEX_HIBERNATE);

                FramedGraphQuery systemIDQuery = event.getGraphContext().getQuery().type(DoctypeMetaModel.class)
                            .has(DoctypeMetaModel.PROPERTY_SYSTEM_ID, Text.REGEX, REGEX_HIBERNATE);
                GremlinPipeline<Vertex, Vertex> systemIdPipeline = new GremlinPipeline<>(systemIDQuery.vertices());

                pipeline.add(systemIdPipeline);

                pipeline.dedup();
            }
        };

        return Query.fromType(DoctypeMetaModel.class).piped(doctypeSearchCriterion);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, DoctypeMetaModel payload)
    {
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        HibernateMappingFileService hibernateMappingFileService = new HibernateMappingFileService(
                    event.getGraphContext());
        HibernateEntityService hibernateEntityService = new HibernateEntityService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        String publicId = payload.getPublicId();
        String systemId = payload.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = extractVersion(publicId, systemId);

        for (XmlFileModel xml : payload.getXmlResources())
        {
            // create a facet, and then identify the XML.
            HibernateMappingFileModel hibernateMapping = hibernateMappingFileService.addTypeToModel(xml);

            Document doc = xmlFileService.loadDocumentQuiet(event, context, hibernateMapping);

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

            technologyTagService.addTagToFileModel(xml, TECH_TAG, TECH_TAG_LEVEL);

            // prepend with package name.
            if (StringUtils.isNotBlank(clzPkg) && !StringUtils.startsWith(clzName, clzPkg))
            {
                clzName = clzPkg + "." + clzName;
            }

            // get a reference to the Java class.
            JavaClassModel clz = javaClassService.getOrCreatePhantom(clzName);

            // create the hibernate facet.
            HibernateEntityModel hibernateEntity = hibernateEntityService.create();
            hibernateEntity.setSpecificationVersion(versionInformation);
            hibernateEntity.setApplications(ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), xml.getProjectModel()));
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
