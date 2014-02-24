package org.jboss.windup.engine.visitor;

import static org.joox.JOOX.$;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JavaClassDaoBean;
import org.jboss.windup.graph.dao.SpringBeanDaoBean;
import org.jboss.windup.graph.dao.SpringConfigurationDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
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
public class SpringConfigurationVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(SpringConfigurationVisitor.class);

	@Inject
	private XmlResourceDaoBean xmlResourceDao;
	
	@Inject
	private SpringConfigurationDaoBean springConfigurationDao;
	
	@Inject
	private SpringBeanDaoBean springBeanDao;
	
	@Inject
	private JavaClassDaoBean javaClassDao;
	
	@Override
	public void run() {
		for(XmlResource entry : xmlResourceDao.findByRootTag("beans")) {
			visitXmlResource(entry);
			xmlResourceDao.commit();
		}
	}
	
	@Override
	public void visitXmlResource(XmlResource entry) {
		try {
			Document doc = xmlResourceDao.asDocument(entry);
			org.w3c.dom.Element element = $(doc).namespace("s", "http://www.springframework.org/schema/beans").xpath("/s:beans").get().get(0);

			if(element != null) {
				SpringConfigurationFacet facet = springConfigurationDao.create(null);
				facet.setXmlFacet(entry);
				
				List<Element> beans = $(element).children("bean").get();
				for(Element bean : beans) {
					String clz = $(bean).attr("class");
					String id = $(bean).attr("id");
					String name = $(bean).attr("name");
					
					if(StringUtils.isBlank(id) && StringUtils.isNotBlank(name)) 
					{
						id = name;
					}
					if(StringUtils.isBlank(clz)) {
						LOG.warn("Spring Bean did not include class:"+$(bean).toString());
						continue;
					}
					
					SpringBeanFacet springBeanRef = springBeanDao.create(null);
					
					if(StringUtils.isNotBlank(id)) {
						springBeanRef.setSpringBeanName(id);
					}
					
					JavaClass classReference = javaClassDao.getJavaClass(clz);
					springBeanRef.setJavaClassFacet(classReference);
					facet.addSpringBeanReference(springBeanRef);
				}
			}
			else {
				LOG.warn("Found [beans] XML without namespace.");
			}
		}
		catch(Exception e) {
			LOG.error("Error.",e);
		}
		
		
		
	}
	
}
