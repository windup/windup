package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.attribute.Text;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateSessionFactoryModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.util.HibernateDialectDataSourceTypeResolver;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers Hibernate Configuration Files (eg, hibernate.cfg.xml), extracts their metadata, and places this metadata into the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover hibernate.cfg.xml files")
public class DiscoverHibernateConfigurationRuleProvider extends IteratingRuleProvider<DoctypeMetaModel> {
    private static final String TECH_TAG = "Hibernate Cfg";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    private static final String REGEX_HIBERNATE = "(?i).*hibernate.configuration.*";
    private static final String JTA_HIBERNATE_PLATFORM = "transaction.jta.platform";

    @Override
    public ConditionBuilder when() {
        QueryGremlinCriterion doctypeSearchCriterion = new QueryGremlinCriterion() {
            @Override
            public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
                pipeline.has(DoctypeMetaModel.PROPERTY_PUBLIC_ID, Text.textRegex(REGEX_HIBERNATE));

                Traversal<?, ?> systemIDQuery = event.getGraphContext().getQuery(DoctypeMetaModel.class)
                        .getRawTraversal()
                        .has(DoctypeMetaModel.PROPERTY_SYSTEM_ID, Text.textRegex(REGEX_HIBERNATE));
                GraphTraversal<Vertex, Vertex> systemIdPipeline = new GraphTraversalSource(event.getGraphContext().getGraph()).V(systemIDQuery.toList());

                pipeline.union(systemIdPipeline);

                pipeline.dedup();
            }
        };

        return Query.fromType(DoctypeMetaModel.class).piped(doctypeSearchCriterion);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, DoctypeMetaModel payload) {
        String publicId = payload.getPublicId();
        String systemId = payload.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = extractVersion(publicId, systemId);

        for (XmlFileModel xml : payload.getXmlResources()) {
            createHibernateConfigurationModel(event, context, xml, versionInformation);
        }
    }

    private void createHibernateConfigurationModel(GraphRewrite event, EvaluationContext context, XmlFileModel xmlFileModel,
                                                   String versionInformation) {
        GraphContext graphContext = event.getGraphContext();
        DataSourceService dataSourceService = new DataSourceService(graphContext);
        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(graphContext);
        GraphService<HibernateSessionFactoryModel> hibernateSessionFactoryService = new GraphService<>(graphContext,
                HibernateSessionFactoryModel.class);
        TechnologyTagService technologyTagService = new TechnologyTagService(graphContext);

        // check the root XML node.
        HibernateConfigurationFileModel hibernateConfigurationModel = hibernateConfigurationFileService.addTypeToModel(xmlFileModel);
        technologyTagService.addTagToFileModel(hibernateConfigurationModel, TECH_TAG, TECH_TAG_LEVEL);

        if (StringUtils.isNotBlank(versionInformation)) {
            hibernateConfigurationModel.setSpecificationVersion(versionInformation);
        }

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), xmlFileModel.getProjectModel());
        Document doc = new XmlFileService(graphContext).loadDocumentQuiet(event, context, xmlFileModel);
        for (Element element : $(doc).find("session-factory").get()) {
            HibernateSessionFactoryModel sessionFactoryModel = hibernateSessionFactoryService.create();
            hibernateConfigurationModel.addHibernateSessionFactory(sessionFactoryModel);
            Map<String, String> sessionFactoryProperties = new HashMap<>();
            for (Element propElement : $(element).find("property")) {
                String propKey = $(propElement).attr("name");
                String propValue = $(propElement).text().trim();
                sessionFactoryProperties.put(propKey, propValue);
            }
            sessionFactoryModel.setSessionFactoryProperties(sessionFactoryProperties);

            // create the datasource references.
            if (sessionFactoryProperties.containsKey("connection.datasource")) {
                final String dataSourceJndiName = sessionFactoryProperties.get("connection.datasource");
                String dataSourceName = dataSourceJndiName;
                if (StringUtils.contains(dataSourceName, "/")) {
                    dataSourceName = StringUtils.substringAfterLast(dataSourceName, "/");
                }

                DataSourceModel dataSource = dataSourceService.createUnique(applications, dataSourceName, dataSourceJndiName);

                if (sessionFactoryProperties.containsKey("dialect")) {
                    String dialect = sessionFactoryProperties.get("dialect");
                    String resolvedType = HibernateDialectDataSourceTypeResolver.resolveDataSourceTypeFromDialect(dialect);
                    if (StringUtils.isNotBlank(resolvedType)) {
                        dataSource.setDatabaseTypeName(resolvedType);
                    }
                }

                if (sessionFactoryProperties.containsKey(JTA_HIBERNATE_PLATFORM)) {
                    dataSource.setXa(true);
                }
            }

        }
    }

    private String extractVersion(String publicId, String systemId) {
        Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z.-]+");

        if (StringUtils.isNotBlank(publicId)) {
            Matcher matcher = pattern.matcher(publicId);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        if (StringUtils.isNotBlank(systemId)) {
            Matcher matcher = pattern.matcher(systemId);
            if (matcher.find()) {
                String match = matcher.group();

                // for system ID, make sure to remove the ".dtd" that could come in.
                return StringUtils.removeEnd(match, ".dtd");
            }
        }

        return null;
    }

}
