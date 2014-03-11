package org.jboss.windup.engine.visitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.base.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.HibernateConfigurationDaoBean;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goes over all XML files that contain Spring namespace and checks root tag, then adds Spring facet. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class HibernateConfigurationVisitor extends AbstractGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateConfigurationVisitor.class);

	private static final String hibernateRegex = "(?i).*hibernate.configuration.*";
	
	@Inject
	private DoctypeDaoBean doctypeDao;
	
	@Inject
	private HibernateConfigurationDaoBean hibernateConfigurationDao;
	
	@Override
	public void run() {
		//visit all Doctypes matching Hibernate in system or public ids.
		long total = doctypeDao.count(doctypeDao.findSystemIdOrPublicIdMatchingRegex(hibernateRegex));
		
		int i=1;
		for(DoctypeMeta doctype : doctypeDao.findSystemIdOrPublicIdMatchingRegex(hibernateRegex)) {
			i++;
			LOG.info("Processed "+i+" of "+" Doctypes.");
			visitDoctype(doctype);
		}
	}
	
	@Override
	public void visitDoctype(DoctypeMeta entry) {
		LOG.info("Doctype: ");
		LOG.info("  - publicId ["+ entry.getPublicId() + "]");
		LOG.info("  - systemId ["+ entry.getSystemId() + "]");
		
		String publicId = entry.getPublicId();
		String systemId = entry.getSystemId();
		
		//extract the version information from the public / system ID.
		
		
		String versionInformation = extractVersion(publicId, systemId);
		
		
		for(XmlResource xml : entry.getXmlResources()) {
			//check the root XML node.
			HibernateConfigurationFacet facet = hibernateConfigurationDao.create();
			facet.setXmlFacet(xml);
			
			if(StringUtils.isNotBlank(versionInformation)) {
				facet.setSpecificationVersion(versionInformation);
			}
		}
	}
	
	protected String extractVersion(String publicId, String systemId) {
		Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z.-]+");
		
		if(StringUtils.isNotBlank(publicId)) {
			Matcher matcher = pattern.matcher(publicId);
			if(matcher.find()) {
				return matcher.group();
			}
		}
		
		if(StringUtils.isNotBlank(systemId)) {
			Matcher matcher = pattern.matcher(systemId);
			if(matcher.find()) {
				String match = matcher.group();
				
				//for system ID, make sure to remove the ".dtd" that could come in.
				return StringUtils.removeEnd(match, ".dtd");
			}
		}
		
		return null;
	}
}
