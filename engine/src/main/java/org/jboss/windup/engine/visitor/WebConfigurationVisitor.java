package org.jboss.windup.engine.visitor;

import static org.joox.JOOX.$;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.xml.DoctypeUtils;
import org.jboss.windup.engine.util.xml.NamespaceUtils;
import org.jboss.windup.engine.visitor.base.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.EnvironmentReferenceDaoBean;
import org.jboss.windup.graph.dao.WebConfigurationDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.meta.EnvironmentReference;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacet;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Goes over all XML files that contain Web App tags, then adds Web facet.
 * 
 *  Extracts Version information.
 *  Extracts Environment References.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class WebConfigurationVisitor extends AbstractGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(WebConfigurationVisitor.class);

	private static final String dtdRegex = "(?i).*web.application.*";
	
	@Inject
	private EnvironmentReferenceDaoBean envRefDao;
	
	@Inject
	private WebConfigurationDaoBean webConfigurationDao;

	@Inject
	private XmlResourceDaoBean xmlDao;
	
	
	@Override
	public void run() {
		//if the XML contains the root tag "web-app"
		for(XmlResource xml : xmlDao.findByRootTag("web-app")) {
			Document doc = xmlDao.asDocument(xml);
			String version = null;
			
			//check it's doctype against the known doctype.
			if(xml.getDoctype() != null) {
				//if it isn't matching doctype, then continue.
				if(!processDoctypeMatches(xml.getDoctype())) {
					continue;
				}
				version = processDoctypeVersion(xml.getDoctype());
			}
			else {
				//if there is no doctype, check the XMD..
				version = $(doc).find("web-app").first().attr("version");

				//if the version attribute isn't found, then grab it from the XSD name if we can.
				if(StringUtils.isBlank(version)) {
					//get the first tag's namespace...
					String namespace = $(doc).find("web-app").namespaceURI();
					if(StringUtils.isBlank(namespace)) {
						namespace = doc.getFirstChild().getNamespaceURI();
					}
					//find that namespace, and try and pull the version from the XSD name...
					for(NamespaceMeta ns: xml.getNamespaces()) {
						if(StringUtils.equals(ns.getURI(), namespace)) {
							version = NamespaceUtils.extractVersion(ns.getSchemaLocation());
							break;
						}
					}
				}
			}
			
			visitXmlResource(xml, doc, version);
		}
	}
	
	public void visitXmlResource(XmlResource xml, Document doc, String versionInformation) {
		//check the root XML node.
		WebConfigurationFacet facet = webConfigurationDao.create();
		facet.setXmlFacet(xml);
		
		//change "_" in the version to "."
		if(StringUtils.isNotBlank(versionInformation)) {
			versionInformation = StringUtils.replace(versionInformation, "_", ".");
			facet.setSpecificationVersion(versionInformation);
		}
		
		String displayName = $(doc).child("display-name").text();
		displayName = StringUtils.trimToNull(displayName);
		if(StringUtils.isNotBlank(displayName)) {
			facet.setDisplayName(displayName);
		}
		
		
		//extract references.
		List<EnvironmentReference> refs = processEnvironmentReference(doc.getDocumentElement());
		for(EnvironmentReference ref : refs) {
			facet.addMeta(ref);
		}
	}
	
	public boolean processDoctypeMatches(DoctypeMeta entry) {
		if(StringUtils.isNotBlank(entry.getPublicId())) {
			if(Pattern.matches(dtdRegex, entry.getPublicId())) {
				return true;
			}
		}

		if(StringUtils.isNotBlank(entry.getSystemId())) {
			if(Pattern.matches(dtdRegex, entry.getSystemId())) {
				return true;
			}
			
		}
		return false;
	}
	
	public String processDoctypeVersion(DoctypeMeta entry) {
		String publicId = entry.getPublicId();
		String systemId = entry.getSystemId();
		
		//extract the version information from the public / system ID.
		String versionInformation = DoctypeUtils.extractVersion(publicId, systemId);
		return versionInformation;
	}
	
	
	
	protected List<EnvironmentReference> processEnvironmentReference(Element element) {
		List<EnvironmentReference> resources = new LinkedList<EnvironmentReference>();
		
		//find JMS references...
		List<Element> queueReferences = $(element).find("resource-ref").get();
		for(Element e : queueReferences) {
			String id = $(e).attr("id");
			String type = $(e).child("res-type").text();
			String name = $(e).child("res-ref-name").text();
			
			type = StringUtils.trim(type);
			name = StringUtils.trim(name);
			
			EnvironmentReference ref = envRefDao.createEnvironmentReference(name, type);
			ref.setReferenceId(id);
			resources.add(ref);
		}
	
		return resources;
	}
	
	

	protected String extractAttributeAndTrim(Element element, String property) {
		String result = $(element).attr(property);
		return StringUtils.trimToNull(result);
	}
	
	protected String extractChildTagAndTrim(Element element, String property) {
		String result = $(element).find(property).first().text();
		return StringUtils.trimToNull(result);
	}
}
