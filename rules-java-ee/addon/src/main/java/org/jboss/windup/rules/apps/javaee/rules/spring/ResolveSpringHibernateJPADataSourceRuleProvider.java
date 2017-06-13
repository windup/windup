package org.jboss.windup.rules.apps.javaee.rules.spring;

import static org.joox.JOOX.$;
import static org.joox.JOOX.attr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.association.JNDIReferenceModel;
import org.jboss.windup.rules.apps.javaee.rules.DiscoverSpringConfigurationFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.rules.apps.javaee.util.HibernateDialectDataSourceTypeResolver;
import org.jboss.windup.rules.apps.javaee.util.SpringDataSourceTypeResolver;
import org.joox.Context;
import org.joox.FastFilter;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers server resource types from specific Spring bean types eg. Hibernate Dialect in LocalSessionFactoryBean -> Spring Bean JNDI resource ->
 * Oracle Data Source
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverSpringConfigurationFilesRuleProvider.class, perform = "Resolve Spring JNDI to DataSource")
public class ResolveSpringHibernateJPADataSourceRuleProvider extends IteratingRuleProvider<SpringBeanModel>
{
    private static final Logger LOG = Logger.getLogger(ResolveSpringHibernateJPADataSourceRuleProvider.class.getName());

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(SpringBeanModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, SpringBeanModel payload)
    {
        // handles only xml based spring beans with certain java classes, such as LocalSessionFactoryBean
        if (payload.getSpringConfiguration() == null || payload.getJavaClass() == null
                    || !isLocalSessionFactoryBean(payload.getJavaClass().getQualifiedName()))
        {
            return;
        }

        SpringConfigurationFileModel springConfig = payload.getSpringConfiguration();
        Document doc = springConfig.asDocument();

        if (doc == null)
        {
            LOG.warning("Document corrupt. Skipping.");
            return;
        }

        for (Element bean : $(doc).find("bean").filter(springid(payload.getSpringBeanName())))
        {
            String dsBeanNameRef = extractJndiRefBeanName(bean);
            if (StringUtils.isBlank(dsBeanNameRef))
            {
                continue;
            }

            String hibernateDialect = null;
            Map<String, String> hibernateProperties = extractProperties(doc, bean);
            hibernateProperties.putAll(extractHibernateJpaVendorJpaProperties(doc, bean));
            if (hibernateProperties.containsKey("hibernate.dialect"))
            {
                hibernateDialect = hibernateProperties.get("hibernate.dialect");
            }

            String springDbName = extractHibernateJpaVendorDatabase(doc, bean);
            processHibernateSessionFactoryBean(event, dsBeanNameRef, hibernateDialect, springDbName);
            return;
        }
        LOG.warning("Did not find bean: " + payload.getSpringBeanName() + " to process within: " + springConfig.getFileName());
    }

    private boolean isLocalSessionFactoryBean(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return false;
        }
        return (qualifiedName.equals("org.springframework.orm.hibernate3.LocalSessionFactoryBean")
        			|| qualifiedName.equals("org.springframework.orm.hibernate3.AbstractSessionFactoryBean")
        			|| qualifiedName.equals("org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.hibernate4.LocalSessionFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.hibernate5.LocalSessionFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.jpa.LocalEntityManagerFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"));
    }

    /*
     * <bean id="jpaVendorAdapter2" class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter"> <property name="dataSource" value="HSQL"/>
     * </bean>
     */
    private String extractHibernateJpaVendorDatabase(Document doc, Element bean)
    {
        for (Element jpaVendorAdapterProperty : $(bean).children("property").filter(attr("name", "jpaVendorAdapter")).get())
        {
            String propertyRef = $(jpaVendorAdapterProperty).attr("ref");
            if (StringUtils.isNotBlank(propertyRef))
            {
                // look for the properties referenced by a local bean..
                for (Element jpaVendorAdapter : findLocalBeanById(doc, propertyRef))
                {
                    // check attribute on element
                    String propAttrValue = $(jpaVendorAdapter).attr("database");
                    if (StringUtils.isNotBlank(propAttrValue))
                    {
                        return propAttrValue;
                    }

                    // now look for the property "dataSource" off of that bean.
                    for (Element p : $(jpaVendorAdapter).children("property").filter(attr("name", "database")).get())
                    {
                        String value = $(p).attr("value");
                        if (StringUtils.isNotBlank(value))
                        {
                            return value;
                        }
                    }
                }
            }
        }

        return null;
    }

    private Map<String, String> extractHibernateJpaVendorJpaProperties(Document doc, Element bean)
    {
        Map<String, String> properties = new HashMap<>();
        for (Element jpaVendorAdapterProperty : $(bean).children("property").filter(attr("name", "jpaVendorAdapter")).get())
        {
            String propertyRef = $(jpaVendorAdapterProperty).attr("ref");
            if (StringUtils.isNotBlank(propertyRef))
            {
                // look for the properties referenced by a local bean..
                for (Element jpaVendorAdapter : findLocalBeanById(doc, propertyRef))
                {
                    properties = extractProperties(doc, jpaVendorAdapter);

                    // read the hibernate dialect
                    for (Element p : $(jpaVendorAdapter).children("property").filter(attr("name", "databasePlatform")).get())
                    {
                        String dialect = $(p).attr("value");
                        if (StringUtils.isNotBlank(dialect))
                        {
                            if (!properties.containsKey("hibernate.dialect"))
                            {
                                properties.put("hibernate.dialect", dialect);
                            }
                        }
                    }
                }
            }
        }
        return properties;
    }

    private Map<String, String> extractProperties(Document doc, Element bean)
    {
        Map<String, String> properties = new HashMap<>();
        for (Element p : $(bean).children("property").filter(attr("name", "hibernateProperties", "jpaProperties", "jpaPropertyMap")).get())
        {
            // first, check to see if it uses a ref attribute...
            String propertyRef = $(p).attr("ref");
            if (StringUtils.isNotBlank(propertyRef))
            {
                // look for the properties referenced by a local bean..
                for (Element ref : findLocalBeanById(doc, propertyRef))
                {
                    properties.putAll(readProperties(ref));
                }
            }
            else
            {
                properties.putAll(readProperties(p));
            }
        }
        return properties;
    }

    private Iterable<Element> findLocalBeanById(Document doc, String id)
    {
        List<Element> elements = new LinkedList<>();
        elements.addAll($(doc).children().filter(attr("id", id)).get());

        if (elements.isEmpty())
        {
            elements.addAll($(doc).children().filter(attr("name", id)).get());
        }
        return elements;
    }

    private String extractJndiRefBeanName(Element bean)
    {
        for (Element dataSource : $(bean).children("property").filter(attr("name", "dataSource")).get())
        {
            // read ref...
            String jndiRef = $(dataSource).attr("ref");
            if (StringUtils.isBlank(jndiRef))
            {
                LOG.info("Looking at ref child of property tag...");
                //look to see if the ref is a child of the property tag...
                jndiRef = $(dataSource).child("ref").attr("bean");
            }
            if(StringUtils.isNotBlank(jndiRef)) {
                return jndiRef;
            }
        }

        return null;
    }

    /*
     * Reads Spring maps, properties, and value pair xml
     */
    private Map<String, String> readProperties(Element properties)
    {
        Map<String, String> values = new HashMap<>();
        for (Element p : $(properties).find("prop"))
        {
            String key = $(p).attr("key");
            String val = $(p).text();
            values.put(key, val);
        }
        for (Element p : $(properties).find("entry"))
        {
            String key = $(p).attr("key");
            String val = $(p).attr("value");
            values.put(key, val);
        }
        for (Element p : $(properties).find("value"))
        {
            String propVal = StringUtils.trim($(p).text());
            String key = StringUtils.substringBefore(propVal, "=");
            String val = StringUtils.substringAfter(propVal, "=");
            values.put(key, val);
        }
        return values;
    }

    /*
     * Takes a JNDI reference and turns the JNDI reference into a database, typing the database based on either the hibernate dialect or the spring
     * database name
     */
    private void processHibernateSessionFactoryBean(GraphRewrite event, String dsBeanName, String hibernateDialect, String springDatabaseName)
    {
        LOG.info("DS Name: " + dsBeanName + ", " + hibernateDialect + ", " + springDatabaseName);
        SpringBeanService springBeanService = new SpringBeanService(event.getGraphContext());
        DataSourceService dataSourceService = new DataSourceService(event.getGraphContext());

        for (SpringBeanModel model : springBeanService.findAllBySpringBeanName(dsBeanName))
        {
            if (model instanceof JNDIReferenceModel && ((JNDIReferenceModel) model).getJndiReference() != null)
            {
                // then this is likely a datasource; set JNDI to Datasource
                JNDIReferenceModel ref = (JNDIReferenceModel) model;
                DataSourceModel dataSource = dataSourceService.addTypeToModel(ref.getJndiReference());

                if (StringUtils.isNotBlank(hibernateDialect))
                {
                    LOG.info(" - Resolved Hibernate dialect: " + hibernateDialect);
                    String resolvedType = HibernateDialectDataSourceTypeResolver.resolveDataSourceTypeFromDialect(hibernateDialect);
                    if (StringUtils.isNotBlank(resolvedType))
                    {
                        dataSource.setDatabaseTypeName(resolvedType);
                    }
                }
                else if (StringUtils.isNotBlank(springDatabaseName))
                {
                    LOG.info(" - Resolved Spring database type: " + springDatabaseName);
                    String resolvedType = SpringDataSourceTypeResolver.resolveDataSourceTypeFromDialect(springDatabaseName);
                    if (StringUtils.isNotBlank(resolvedType))
                    {
                        dataSource.setDatabaseTypeName(resolvedType);
                    }
                }

            }
            else
            {
                LOG.warning("Not JNDI Reference.");
            }
        }
    }

    /*
     * Filters Joox by the spring bean's name (name or id) leveraging name to support legacy spring
     */
    public static FastFilter springid(final String id)
    {
        return new FastFilter()
        {
            @Override
            public boolean filter(Context context)
            {
                String name = $(context).attr("name");
                String idVal = $(context).attr("id");

                LOG.info("Matching: " + id + " Against -- ID: " + idVal + " Name: " + name);

                return (StringUtils.equals(id, idVal) || StringUtils.equals(id, name));
            }
        };
    }
}
