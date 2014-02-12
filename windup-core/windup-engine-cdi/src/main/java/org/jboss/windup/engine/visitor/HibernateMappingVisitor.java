package org.jboss.windup.engine.visitor;

import static org.joox.JOOX.$;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.xml.XmlUtil;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.HibernateConfigurationDaoBean;
import org.jboss.windup.graph.dao.HibernateEntityDaoBean;
import org.jboss.windup.graph.dao.JavaClassDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Goes over all XML files that contain Spring namespace and checks root tag, then adds Spring facet. 
 * 
 * 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class HibernateMappingVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(HibernateMappingVisitor.class);

	private static final String hibernateRegex = "(?i).*hibernate.mapping.*";
	
	@Inject
	private DoctypeDaoBean doctypeDao;
	
	@Inject
	private HibernateEntityDaoBean hibernateEntityDao;
	
	@Inject
	private JavaClassDaoBean javaClassDao;
	
	@Inject
	private XmlResourceDaoBean xmlResourceDao;
	
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
			Document doc = xmlResourceDao.asDocument(xml);
			
			if(!XmlUtil.xpathExists(doc, "/hibernate-mapping", null)) {
				LOG.warn("Docment does not contain Hibernate Mapping.");
				continue;
			}
			
			String clzPkg = $(doc).xpath("/hibernate-mapping").attr("package");
			String clzName = $(doc).xpath("/hibernate-mapping/class").attr("name");
			String tableName = $(doc).xpath("/hibernate-mapping/class").attr("table");
			String schemaName = $(doc).xpath("/hibernate-mapping/class").attr("schema");
			String catalogName = $(doc).xpath("/hibernate-mapping/class").attr("catalog");
			
			if(StringUtils.isBlank(clzName)) {
				LOG.warn("Docment does not contain class name mapping: "+$(doc).xpath("/hibernate-mapping/class").toString());
				continue;
			}
			
			//prepend with package name.
			if(StringUtils.isNotBlank(clzPkg) && !StringUtils.startsWith(clzName, clzPkg)) {
				clzName = clzPkg+"."+clzName;
			}

			//get a reference to the Java class.
			JavaClass clz = javaClassDao.getJavaClass(clzName);
			

			//create the hibernate facet.
			HibernateEntityFacet facet = hibernateEntityDao.create();
			facet.setSpecificationVersion(versionInformation);
			facet.setJavaClassFacet(clz);
			facet.setTableName(tableName);
			facet.setSchemaName(schemaName);
			facet.setCatalogName(catalogName);
			
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
