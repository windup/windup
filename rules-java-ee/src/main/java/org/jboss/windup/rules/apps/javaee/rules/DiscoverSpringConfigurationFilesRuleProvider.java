package org.jboss.windup.rules.apps.javaee.rules;

import static org.joox.JOOX.$;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.reporting.model.TechnologyTagModel.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.rules.apps.javaee.service.SpringConfigurationFileService;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.XmlFileModel;
import org.jboss.windup.rules.apps.xml.XmlFileService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Discovers spring configuration XML files, and places the metadata into the Graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class DiscoverSpringConfigurationFilesRuleProvider extends IteratingRuleProvider<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverSpringConfigurationFilesRuleProvider.class
                .getSimpleName());

    private static final String TECH_TAG = "Spring XML";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.SUCCESS;

    @Inject
    private XmlFileService xmlFileService;
    @Inject
    private JavaClassService javaClassService;
    @Inject
    private SpringConfigurationFileService springConfigurationFileService;
    @Inject
    private SpringBeanService springBeanService;
    @Inject
    private TechnologyTagService technologyTagService;

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
        return Query.find(XmlFileModel.class).withProperty(XmlFileModel.ROOT_TAG_NAME, "beans");
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
    {
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

            JavaClassModel classReference = javaClassService.getOrCreate(clz);
            springBeanRef.setJavaClass(classReference);

            springConfigurationModel.addSpringBeanReference(springBeanRef);
        }
    }
}
