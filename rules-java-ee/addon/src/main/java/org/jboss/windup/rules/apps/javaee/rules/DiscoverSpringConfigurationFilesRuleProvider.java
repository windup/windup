package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;
import static org.joox.JOOX.attr;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.association.JNDIReferenceModel;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.rules.apps.javaee.service.SpringConfigurationFileService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers spring configuration XML files, and places the metadata into the Graph.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class DiscoverSpringConfigurationFilesRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverSpringConfigurationFilesRuleProvider.class
                .getSimpleName());

    private static final String TECH_TAG = "Spring XML";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    public DiscoverSpringConfigurationFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverSpringConfigurationFilesRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "Discover Spring Config Files";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "beans");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        SpringConfigurationFileService springConfigurationFileService = new SpringConfigurationFileService(
                    event.getGraphContext());
        SpringBeanService springBeanService = new SpringBeanService(event.getGraphContext());
        JNDIResourceService jndiResourceService = new JNDIResourceService(event.getGraphContext());

        Document doc = xmlFileService.loadDocumentQuiet(payload);
        if (doc == null)
        {
            // skip if the xml failed to load
            return;
        }
        List<Element> beansElements = $(doc).namespace("s", "http://www.springframework.org/schema/beans")
                    .xpath("/s:beans").get();

        if (beansElements.size() == 0)
        {
            LOG.log(Level.WARNING, "Found [beans] XML without namespace at: " + payload.getFilePath() + ".");
            return;
        }

        technologyTagService.addTagToFileModel(payload, TECH_TAG, TECH_TAG_LEVEL);

        Element element = beansElements.get(0);
        SpringConfigurationFileModel springConfigurationModel = springConfigurationFileService
                    .addTypeToModel(payload);

        // create bean models
        List<Element> beans = $(element).children("bean").get();
        for (Element bean : beans)
        {
            String clz = $(bean).attr("class");
            String id = $(bean).attr("id");
            String name = $(bean).attr("name");

            if (StringUtils.isBlank(id) && StringUtils.isNotBlank(name))
            {
                id = name;
            }
            if (StringUtils.isBlank(clz))
            {
                LOG.log(Level.WARNING, "Spring Bean did not include class:" + $(bean).toString());
                continue;
            }

            SpringBeanModel springBeanRef = springBeanService.create();

            if (StringUtils.isNotBlank(id))
            {
                springBeanRef.setSpringBeanName(id);
            }

            JavaClassModel classReference = javaClassService.getOrCreatePhantom(clz);
            springBeanRef.setJavaClass(classReference);

            springConfigurationModel.addSpringBeanReference(springBeanRef);

            // find out if the class is a JndiObjectFactoryBean, extract the JNDI & type the JNDI resource, when expectedType property is provided.
            /*
             * <bean id="beanDataSource" class="org.springframework.jndi.JndiObjectFactoryBean"> <property name="jndiName"
             * value="jdbc/ExampleSpringBeanDataSource"/> <property name="expectedType" value="javax.sql.DataSource"/> </bean>
             */
            if (StringUtils.isNotBlank(clz) && StringUtils.equals("org.springframework.jndi.JndiObjectFactoryBean", clz))
            {
                String expectedType = $(bean).children("property").filter(attr("name", "expectedType")).first().attr("value");
                String jndiName = $(bean).children("property").filter(attr("name", "jndiName")).first().attr("value");

                LOG.info("Found JNDI in Bean Spring: " + jndiName);
                if (StringUtils.isNotBlank(jndiName))
                {
                    JNDIResourceModel jndiResource = jndiResourceService.createUnique(jndiName);
                    if (StringUtils.isNotBlank(expectedType))
                    {
                        LOG.info(" -- Type: " + expectedType);
                        jndiResourceService.associateTypeJndiResource(jndiResource, expectedType);
                    }

                    JNDIReferenceModel reference = GraphService.addTypeToModel(event.getGraphContext(), springBeanRef, JNDIReferenceModel.class);
                    reference.setJndiReference(jndiResource);
                }
            }
        }

        // extract JNDI references & type the JNDI resource, when expected-type is provided.
        /*
         * <jee:jndi-lookup id="jeeDataSource" jndi-name="jdbc/ExampleSpringJNDIDataSource" expected-type="javax.sql.DataSource" />
         */
        List<Element> jndis = $(element).children("jndi-lookup").get();
        if (jndis.size() > 0)
        {
            for (Element jndi : jndis)
            {
                String id = $(jndi).attr("id");
                String jndiName = $(jndi).attr("jndi-name");
                String expectedType = $(jndi).attr("expected-type");

                LOG.info("Found JNDI in JEE Spring: " + jndiName);

                SpringBeanModel springBeanRef = springBeanService.create();
                springBeanRef.setSpringBeanName(id);
                
                JNDIResourceModel jndiResource = null;
                if (StringUtils.isNotBlank(jndiName))
                {
                    jndiResource = jndiResourceService.createUnique(jndiName);
                    if (StringUtils.isNotBlank(expectedType))
                    {
                        LOG.info(" -- Type: " + expectedType);
                        jndiResourceService.associateTypeJndiResource(jndiResource, expectedType);
                    }
                    

                    //this will make it so that we have an association from the SpringBean to the JNDI Resource, layered on top of the typical SpringBean.
                    //this in turn will make this both a SpringBeanModel and a JNDIReferenceModel in the graph, so we can specifically look up Spring Bean JNDI References
                    JNDIReferenceModel reference = GraphService.addTypeToModel(event.getGraphContext(), springBeanRef, JNDIReferenceModel.class);
                    reference.setJndiReference(jndiResource);
                }

                //jndi-lookup is shorthand for this class.  register it to allow location of the bean by name to configuration file
                final String clz = "org.springframework.jndi.JndiObjectFactoryBean";
                JavaClassModel classReference = javaClassService.getOrCreatePhantom(clz);
                springBeanRef.setJavaClass(classReference);
                
                
                
                springConfigurationModel.addSpringBeanReference(springBeanRef);
            }
        }
    }
}
