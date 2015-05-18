package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JPAConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPAPersistenceUnitModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.JPAConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.JPAEntityService;
import org.jboss.windup.rules.apps.javaee.service.JPAPersistenceUnitService;
import org.jboss.windup.rules.apps.javaee.util.HibernateDialectDataSourceTypeResolver;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers persistence.xml files and parses the related metadata
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class DiscoverJpaConfigurationXmlRuleProvider extends IteratingRuleProvider<NamespaceMetaModel>
{
    private static final Logger LOG = Logging.get(DiscoverJpaConfigurationXmlRuleProvider.class);

    private static final String TECH_TAG = "JPA XML";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    public DiscoverJpaConfigurationXmlRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverJpaConfigurationXmlRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover JPA Persistence XML Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(NamespaceMetaModel.class).withProperty(NamespaceMetaModel.NAMESPACE_URI, "http://java.sun.com/xml/ns/persistence");
    }

    public void perform(GraphRewrite event, EvaluationContext context, NamespaceMetaModel payload)
    {
        for (XmlFileModel xml : payload.getXmlResources())
        {
            if (StringUtils.equals(xml.getRootTagName(), "persistence"))
            {
                Document doc = new XmlFileService(event.getGraphContext()).loadDocumentQuiet(xml);
                if (doc == null)
                {
                    // failed to parse, skip
                    continue;
                }
                extractMetadata(event.getGraphContext(), xml, doc);
            }
        }
    }

    private void extractMetadata(GraphContext graphContext, XmlFileModel xmlFileModel, Document doc)
    {
        DataSourceService dataSourceService = new DataSourceService(graphContext);
        JavaClassService javaClassService = new JavaClassService(graphContext);
        JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(graphContext);
        JPAPersistenceUnitService jpaPersistenceUnitService = new JPAPersistenceUnitService(graphContext);
        JPAEntityService jpaEntityService = new JPAEntityService(graphContext);

        TechnologyTagService technologyTagService = new TechnologyTagService(graphContext);
        TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(xmlFileModel, TECH_TAG, TECH_TAG_LEVEL);

        String version = $(doc).attr("version");
        if (StringUtils.isNotBlank(version))
        {
            technologyTag.setVersion(version);
        }

        // check the root XML node.
        JPAConfigurationFileModel jpaConfigurationModel = jpaConfigurationFileService.addTypeToModel(xmlFileModel);
        if (StringUtils.isNotBlank(version))
        {
            jpaConfigurationModel.setSpecificationVersion(version);
        }

        for (Element element : $(doc).find("persistence-unit").get())
        {
            JPAPersistenceUnitModel persistenceUnitModel = jpaPersistenceUnitService.create();
            String persistenceUnitName = $(element).attr("name");
            persistenceUnitModel.setName(persistenceUnitName);

            jpaConfigurationModel.addPersistenceUnit(persistenceUnitModel);

            if ($(element).find("jta-data-source").isNotEmpty())
            {
                final String dataSourceJndiName = $(element).find("jta-data-source").text();
                String dataSourceName = dataSourceJndiName;
                if (StringUtils.contains(dataSourceName, "/"))
                {
                    dataSourceName = StringUtils.substringAfterLast(dataSourceName, "/");
                }

                DataSourceModel dataSource = dataSourceService.createUnique(dataSourceName, dataSourceJndiName);
                persistenceUnitModel.addDataSource(dataSource);
            }

            if ($(element).find("non-jta-data-source").isNotEmpty())
            {
                final String dataSourceJndiName = $(element).find("non-jta-data-source").text();
                String dataSourceName = dataSourceJndiName;
                if (StringUtils.contains(dataSourceName, "/"))
                {
                    dataSourceName = StringUtils.substringAfterLast(dataSourceName, "/");
                }

                DataSourceModel dataSource = dataSourceService.createUnique(dataSourceName, dataSourceJndiName);
                persistenceUnitModel.addDataSource(dataSource);
            }

            for (Element clz : $(element).find("class").get())
            {
                String clzName = $(clz).text();
                JavaClassModel javaClz = javaClassService.getOrCreatePhantom(clzName);

                JPAEntityModel entityModel = jpaEntityService.create();
                entityModel.setJavaClass(javaClz);
            }

            Map<String, String> persistenceUnitProperties = new HashMap<>();
            for (Element propElement : $(element).find("property"))
            {
                String propKey = $(propElement).attr("name");
                String propValue = $(propElement).attr("value");
                persistenceUnitProperties.put(propKey, propValue);
            }

            if (persistenceUnitProperties.containsKey("hibernate.dialect"))
            {
                String dialect = persistenceUnitProperties.get("hibernate.dialect");
                for (DataSourceModel datasource : persistenceUnitModel.getDataSources())
                {
                    datasource.setDatabaseTypeName(HibernateDialectDataSourceTypeResolver.resolveDataSourceTypeFromDialect(dialect));
                }
            }

            persistenceUnitModel.setProperties(persistenceUnitProperties);
        }
    }
}
