package org.jboss.windup.engine.visitor.inspector;

import static org.joox.JOOX.$;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.SpringBeanDao;
import org.jboss.windup.graph.dao.SpringConfigurationDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacetModel;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Goes over all XML files that contain Spring namespace and checks root tag, then adds Spring facet.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class SpringConfigurationVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(SpringConfigurationVisitor.class);

    @Inject
    private XmlResourceDao xmlResourceDao;

    @Inject
    private SpringConfigurationDao springConfigurationDao;

    @Inject
    private SpringBeanDao springBeanDao;

    @Inject
    private JavaClassDao javaClassDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        for (XmlResourceModel entry : xmlResourceDao.findByRootTag("beans"))
        {
            visitXmlResource(entry);
            xmlResourceDao.commit();
        }
    }

    @Override
    public void visitXmlResource(XmlResourceModel entry)
    {
        try
        {
            Document doc = entry.asDocument();
            org.w3c.dom.Element element = $(doc).namespace("s", "http://www.springframework.org/schema/beans")
                        .xpath("/s:beans").get().get(0);

            if (element != null)
            {
                SpringConfigurationFacetModel facet = springConfigurationDao.create();
                facet.setXmlFacet(entry);

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
                        LOG.warn("Spring Bean did not include class:" + $(bean).toString());
                        continue;
                    }

                    SpringBeanFacetModel springBeanRef = springBeanDao.create();

                    if (StringUtils.isNotBlank(id))
                    {
                        springBeanRef.setSpringBeanName(id);
                    }

                    JavaClassModel classReference = javaClassDao.createJavaClass(clz);
                    springBeanRef.setJavaClassFacet(classReference);
                    facet.addSpringBeanReference(springBeanRef);
                }
            }
            else
            {
                LOG.warn("Found [beans] XML without namespace.");
            }
        }
        catch (Exception e)
        {
            LOG.error("Error.", e);
        }

    }

}
