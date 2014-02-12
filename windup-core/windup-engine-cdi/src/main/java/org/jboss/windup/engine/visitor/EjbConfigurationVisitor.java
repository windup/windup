package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.xml.DoctypeUtils;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.EJBConfigurationDaoBean;
import org.jboss.windup.graph.dao.EJBEntityDaoBean;
import org.jboss.windup.graph.dao.EJBSessionBeanDaoBean;
import org.jboss.windup.graph.dao.JavaClassDaoBean;
import org.jboss.windup.graph.dao.MessageDrivenDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Goes over all XML files that contain Enterprise JavaBean doctype and checks root tag, then adds EJB facet. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class EjbConfigurationVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(EjbConfigurationVisitor.class);

	private static final String dtdRegex = "(?i).*enterprise.javabeans.*";
	
	@Inject
	private DoctypeDaoBean doctypeDao;
	
	@Inject
	private EJBConfigurationDaoBean ejbConfigurationDao;

	@Inject
	private XmlResourceDaoBean xmlDao;
	
	@Inject
	private JavaClassDaoBean javaClassDao;
	
	@Inject
	private EJBEntityDaoBean ejbEntityDao;
	
	@Inject
	private MessageDrivenDaoBean mdbDao;
	
	@Inject
	private EJBSessionBeanDaoBean sessionBeanDao;
	
	@Override
	public void run() {
		//visit all Doctypes matching Hibernate in system or public ids.
		long total = doctypeDao.count(doctypeDao.findSystemIdOrPublicIdMatchingRegex(dtdRegex));
		
		int i=1;
		for(DoctypeMeta doctype : doctypeDao.findSystemIdOrPublicIdMatchingRegex(dtdRegex)) {
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
		String versionInformation = DoctypeUtils.extractVersion(publicId, systemId);
		
		for(XmlResource xml : entry.getXmlResources()) {
			Document doc = xmlDao.asDocument(xml);
			//check the root XML node.
			EjbConfigurationFacet facet = ejbConfigurationDao.create();
			facet.setXmlFacet(xml);
			
			if(StringUtils.isNotBlank(versionInformation)) {
				facet.setSpecificationVersion(versionInformation);
			}
			
			//process all session beans...
			for(Element element : $(doc).xpath("//session").get()) {
				processSessionBeanElement(facet, element);
			}
			
			//process all message driven beans...
			for(Element element : $(doc).xpath("//message-driven").get()) {
				processMessageDrivenElement(facet, element);
			}
			
			//process all entity beans...
			for(Element element : $(doc).xpath("//entity").get()) {
				processMessageDrivenElement(facet, element);
			}
		}
	}
	
	protected void processSessionBeanElement(EjbConfigurationFacet ejbConfig, Element element) {
		JavaClass home = null;
		JavaClass localHome = null;
		JavaClass remote = null;
		JavaClass local = null;
		JavaClass ejb = null;
		
		String ejbId = extractAttributeAndTrim(element, "id");
		String displayName = extractChildTagAndTrim(element, "display-name");
		String ejbName = extractChildTagAndTrim(element, "ejb-name");
		
		//get local class.
		String localClz = extractChildTagAndTrim(element, "local");
		if(localClz != null) {
			local = javaClassDao.getJavaClass(localClz);
		}
		
		//get local home class.
		String localHomeClz = extractChildTagAndTrim(element, "local-home");
		if(localHomeClz != null) {
			localHome = javaClassDao.getJavaClass(localHomeClz);
		}
		
		//get home class.
		String homeClz = extractChildTagAndTrim(element, "home");
		if(homeClz != null) {
			home = javaClassDao.getJavaClass(homeClz);
		}
		
		//get remote class.
		String remoteClz = extractChildTagAndTrim(element, "remote");
		if(remoteClz != null) {
			remote = javaClassDao.getJavaClass(remoteClz);
		}
		
		//get the ejb class.
		String ejbClz = extractChildTagAndTrim(element, "ejb-class");
		if(ejbClz != null) {
			ejb = javaClassDao.getJavaClass(ejbClz);
		}
		
		String sessionType = extractChildTagAndTrim(element, "session-type"); 
		String transactionType = extractChildTagAndTrim(element, "transaction-type");
		
		EjbSessionBeanFacet sessionBean = sessionBeanDao.create();
		sessionBean.setEjbId(ejbId);
		sessionBean.setDisplayName(displayName);
		sessionBean.setSessionBeanName(ejbName);
		sessionBean.setEjbLocal(local);
		sessionBean.setEjbLocalHome(localHome);
		sessionBean.setEjbHome(home);
		sessionBean.setEjbRemote(remote);
		sessionBean.setJavaClassFacet(ejb);
		sessionBean.setSessionType(sessionType);
		sessionBean.setTransactionType(transactionType);

		ejbConfig.addEjbSessionBean(sessionBean);
		mdbDao.commit();
	}
	
	protected void processMessageDrivenElement(EjbConfigurationFacet ejbConfig, Element element) {
		JavaClass ejb = null;
		
		String ejbId = extractAttributeAndTrim(element, "id");
		String displayName = extractChildTagAndTrim(element, "display-name");
		String ejbName = extractChildTagAndTrim(element, "ejb-name");
		
		//get the ejb class.
		String ejbClz = extractChildTagAndTrim(element, "ejb-class");
		if(ejbClz != null) {
			ejb = javaClassDao.getJavaClass(ejbClz);
		}
		
		String sessionType = extractChildTagAndTrim(element, "session-type");
		String transactionType = extractChildTagAndTrim(element, "transaction-type");		
		
		if(ejb == null) {
			LOG.warn("Message driven is null.");
			return;
		}
		
		MessageDrivenBeanFacet mdb = mdbDao.create();
		mdb.setJavaClassFacet(ejb);
		mdb.setMessageDrivenBeanName(ejbName);
		mdb.setDisplayName(displayName);
		mdb.setEjbId(ejbId);
		mdb.setSessionType(sessionType);
		mdb.setTransactionType(transactionType);
		
		ejbConfig.addMessageDriven(mdb);
		mdbDao.commit();
	}
	
	protected void processEntityElement(EjbConfigurationFacet ejbConfig, Element element) {
		JavaClass localHome = null;
		JavaClass local = null;
		JavaClass ejb = null;
		
		String ejbId = extractAttributeAndTrim(element, "id");
		String displayName = extractChildTagAndTrim(element, "display-name");
		String ejbName = extractChildTagAndTrim(element, "ejb-name");

		//get local class.
		String localClz = extractChildTagAndTrim(element, "local");
		if(localClz != null) {
			local = javaClassDao.getJavaClass(localClz);
		}
		
		//get local home class.
		String localHomeClz = extractChildTagAndTrim(element, "local-home");
		if(localHomeClz != null) {
			localHome = javaClassDao.getJavaClass(localHomeClz);
		}
		
		//get the ejb class.
		String ejbClz = extractChildTagAndTrim(element, "ejb-class");
		if(ejbClz != null) {
			ejb = javaClassDao.getJavaClass(ejbClz);
		} 
		
		String persistenceType = extractChildTagAndTrim(element, "persistence-type");		

		if(ejb == null) {
			//do nothing.
			LOG.warn("EJB Entity is null.");
			return;
		}
		
		//create new entity facet.
		EjbEntityFacet entity = ejbEntityDao.create();
		entity.setPersistenceType(persistenceType);
		entity.setEjbId(ejbId);
		entity.setDisplayName(displayName);
		entity.setEjbEntityName(ejbName);
		entity.setJavaClassFacet(ejb);
		entity.setEjbLocalHome(localHome);
		entity.setEjbLocal(local);

		
		ejbConfig.addEjbEntity(entity);
		ejbEntityDao.commit();
	}
	

	protected String extractAttributeAndTrim(Element element, String property) {
		String result = $(element).attr(property);
		return StringUtils.trimToNull(result);
	}
	
	protected String extractChildTagAndTrim(Element element, String property) {
		String result = $(element).child(property).first().text();
		return StringUtils.trimToNull(result);
	}
}
