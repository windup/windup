package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.association.JNDIReferenceModel;
import org.jboss.windup.rules.apps.javaee.service.DataSourceService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.rules.apps.javaee.util.HibernateDialectDataSourceTypeResolver;
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
public class ResolveSpringDataSourceRuleProvider extends IteratingRuleProvider<SpringBeanModel>
{
    @Inject
    GraphTypeManager manager;

    private static final Logger LOG = Logger.getLogger(ResolveSpringDataSourceRuleProvider.class
                .getSimpleName());

    public ResolveSpringDataSourceRuleProvider()
    {
        super(MetadataBuilder.forProvider(ResolveSpringDataSourceRuleProvider.class)
                    .addExecuteAfter(DiscoverSpringConfigurationFilesRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Resolve Spring JNDI to DataSource";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(SpringBeanModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, SpringBeanModel payload)
    {
        if (payload.getSpringConfiguration() == null || payload.getJavaClass() == null
                    || !isLocalSessionFactoryBean(payload.getJavaClass().getQualifiedName()))
        {
            LOG.info("Skipping.");
            return;
        }

        SpringConfigurationFileModel springConfig = payload.getSpringConfiguration();
        Document doc = springConfig.asDocument();

        if (doc == null)
        {
            LOG.warning("Document corrupt. Skipping.");
            return;
        }

        // now find the XML element:
        for (Element bean : $(doc).find("bean").get())
        {
            String id = $(bean).attr("id");
            if (StringUtils.isBlank(id))
            {
                id = $(bean).attr("name");
            }

            if (StringUtils.equals(id, payload.getSpringBeanName()))
            {
                LOG.info("Located bean: " + id);
                // found the bean in the XML for further exploration
                processHibernateSessionFactoryBean(event, bean);
                return;
            }
        }
        LOG.warning("Did not find bean: " + payload.getSpringBeanName() + " to process.");
    }

    private boolean isLocalSessionFactoryBean(String qualifiedName)
    {
        if (qualifiedName == null)
        {
            return false;
        }
        return (qualifiedName.equals("org.springframework.orm.hibernate3.LocalSessionFactoryBean")
                    || qualifiedName.equals("org.springframework.orm.hibernate4.LocalSessionFactoryBean"));
    }

    private void processHibernateSessionFactoryBean(GraphRewrite event, Element bean)
    {
        String dsRef = null;
        Map<String, String> hibernateProperties = null;
        for (Element p : $(bean).children("property"))
        {
            String name = $(p).attr("name");

            // if it references another bean, look up that bean and run
            if (StringUtils.equals(name, "dataSource"))
            {
                dsRef = $(p).attr("ref");
                LOG.info(" - Found datasource bean ref: " + dsRef);
            }
            else if (StringUtils.equals(name, "hibernateProperties"))
            {
                hibernateProperties = readProperties(p);
                LOG.info(" - Found hibernate properties with size: " + hibernateProperties.size());
            }
        }

        if (StringUtils.isNotBlank(dsRef))
        {
            processHibernateSessionFactoryBean(event, dsRef, hibernateProperties);
        }

    }

    private Map<String, String> readProperties(Element properties)
    {
        Map<String, String> values = new HashMap<String, String>();
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

    private void processHibernateSessionFactoryBean(GraphRewrite event, String dsBeanName, Map<String, String> hibernateProperties)
    {
        SpringBeanService springBeanService = new SpringBeanService(event.getGraphContext());
        DataSourceService dataSourceService = new DataSourceService(event.getGraphContext());

        for (SpringBeanModel model : springBeanService.findAllBySpringBeanName(dsBeanName))
        {
            if (model instanceof JNDIReferenceModel && ((JNDIReferenceModel) model).getJndiReference() != null)
            {
                // then this is likely a datasource; set JNDI to Datasource
                JNDIReferenceModel ref = (JNDIReferenceModel) model;
                DataSourceModel dataSource = dataSourceService.addTypeToModel(ref.getJndiReference());

                if (hibernateProperties.containsKey("hibernate.dialect"))
                {
                    String dialect = hibernateProperties.get("hibernate.dialect");
                    LOG.info(" - Resolved Hibernate dialect: " + dialect);
                    dataSource.setDatabaseTypeName(HibernateDialectDataSourceTypeResolver.resolveDataSourceTypeFromDialect(dialect));
                }
            }
            else
            {
                LOG.warning("Not JNDI Reference.");
            }
        }
    }
}
