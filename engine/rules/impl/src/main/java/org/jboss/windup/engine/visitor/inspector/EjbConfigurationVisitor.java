package org.jboss.windup.engine.visitor.inspector;

import static org.joox.JOOX.$;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.util.xml.DoctypeUtils;
import org.jboss.windup.engine.util.xml.NamespaceUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.dao.EJBEntityDao;
import org.jboss.windup.graph.dao.EJBSessionBeanDao;
import org.jboss.windup.graph.dao.EnvironmentReferenceDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.MessageDrivenDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.EnvironmentReferenceModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacetModel;
import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Goes over all XML files that contain Enterprise JavaBean doctype and checks root tag, then adds EJB facet.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class EjbConfigurationVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(EjbConfigurationVisitor.class);

    private static final String dtdRegex = "(?i).*enterprise.javabeans.*";

    @Inject
    private EnvironmentReferenceDao envRefDao;

    @Inject
    private EJBConfigurationDao ejbConfigurationDao;

    @Inject
    private XmlResourceDao xmlDao;

    @Inject
    private JavaClassDao javaClassDao;

    @Inject
    private EJBEntityDao ejbEntityDao;

    @Inject
    private MessageDrivenDao mdbDao;

    @Inject
    private EJBSessionBeanDao sessionBeanDao;

    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return generateDependencies(XmlResourceVisitor.class);
    }
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        for (XmlResourceModel xml : xmlDao.findByRootTag("ejb-jar"))
        {
            Document doc = xml.asDocument();

            // otherwise, it is a EJB-JAR XML.
            if (xml.getDoctype() != null)
            {
                // check doctype.
                if (!processDoctypeMatches(xml.getDoctype()))
                {
                    // move to next document.
                    continue;
                }
                String version = processDoctypeVersion(xml.getDoctype());
                visitXmlResource(xml, doc, version);
            }
            else
            {
                String namespace = $(doc).find("ejb-jar").namespaceURI();
                if (StringUtils.isBlank(namespace))
                {
                    namespace = doc.getFirstChild().getNamespaceURI();
                }

                String version = $(doc).find("ejb-jar").first().attr("version");

                // if the version attribute isn't found, then grab it from the XSD name if we can.
                if (StringUtils.isBlank(version))
                {
                    for (NamespaceMetaModel ns : xml.getNamespaces())
                    {
                        LOG.debug("Namespace URI: " + ns.getURI());
                        if (StringUtils.equals(ns.getURI(), namespace))
                        {
                            LOG.debug("Schema Location: " + ns.getSchemaLocation());
                            version = NamespaceUtils.extractVersion(ns.getSchemaLocation());
                            LOG.debug("Version: " + version);
                        }
                    }
                }

                visitXmlResource(xml, doc, version);
            }
        }
    }

    public void visitXmlResource(XmlResourceModel xml, Document doc, String versionInformation)
    {
        // check the root XML node.
        EjbConfigurationFacetModel facet = ejbConfigurationDao.create();
        facet.setXmlFacet(xml);

        if (StringUtils.isNotBlank(versionInformation))
        {
            facet.setSpecificationVersion(versionInformation);
        }

        // process all session beans...
        //
        for (Element element : $(doc).find("session").get())
        {
            processSessionBeanElement(facet, element);
        }

        // process all message driven beans...
        for (Element element : $(doc).find("message-driven").get())
        {
            processMessageDrivenElement(facet, element);
        }

        // process all entity beans...
        for (Element element : $(doc).find("entity").get())
        {
            processMessageDrivenElement(facet, element);
        }
    }

    public boolean processDoctypeMatches(DoctypeMetaModel entry)
    {
        if (StringUtils.isNotBlank(entry.getPublicId()))
        {
            if (Pattern.matches(dtdRegex, entry.getPublicId()))
            {
                return true;
            }
        }

        if (StringUtils.isNotBlank(entry.getSystemId()))
        {
            if (Pattern.matches(dtdRegex, entry.getSystemId()))
            {
                return true;
            }

        }
        return false;
    }

    public String processDoctypeVersion(DoctypeMetaModel entry)
    {
        String publicId = entry.getPublicId();
        String systemId = entry.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = DoctypeUtils.extractVersion(publicId, systemId);
        return versionInformation;
    }

    protected void processSessionBeanElement(EjbConfigurationFacetModel ejbConfig, Element element)
    {
        JavaClassModel home = null;
        JavaClassModel localHome = null;
        JavaClassModel remote = null;
        JavaClassModel local = null;
        JavaClassModel ejb = null;

        String ejbId = extractAttributeAndTrim(element, "id");
        String displayName = extractChildTagAndTrim(element, "display-name");
        String ejbName = extractChildTagAndTrim(element, "ejb-name");

        // get local class.
        String localClz = extractChildTagAndTrim(element, "local");
        if (localClz != null)
        {
            local = javaClassDao.createJavaClass(localClz);
        }

        // get local home class.
        String localHomeClz = extractChildTagAndTrim(element, "local-home");
        if (localHomeClz != null)
        {
            localHome = javaClassDao.createJavaClass(localHomeClz);
        }

        // get home class.
        String homeClz = extractChildTagAndTrim(element, "home");
        if (homeClz != null)
        {
            home = javaClassDao.createJavaClass(homeClz);
        }

        // get remote class.
        String remoteClz = extractChildTagAndTrim(element, "remote");
        if (remoteClz != null)
        {
            remote = javaClassDao.createJavaClass(remoteClz);
        }

        // get the ejb class.
        String ejbClz = extractChildTagAndTrim(element, "ejb-class");
        if (ejbClz != null)
        {
            ejb = javaClassDao.createJavaClass(ejbClz);
        }

        String sessionType = extractChildTagAndTrim(element, "session-type");
        String transactionType = extractChildTagAndTrim(element, "transaction-type");

        EjbSessionBeanFacetModel sessionBean = sessionBeanDao.create();
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

        List<EnvironmentReferenceModel> refs = processEnvironmentReference(element);
        for (EnvironmentReferenceModel ref : refs)
        {
            sessionBean.addMeta(ref);
        }

        ejbConfig.addEjbSessionBean(sessionBean);
        mdbDao.commit();
    }

    protected void processMessageDrivenElement(EjbConfigurationFacetModel ejbConfig, Element element)
    {
        JavaClassModel ejb = null;

        String ejbId = extractAttributeAndTrim(element, "id");
        String displayName = extractChildTagAndTrim(element, "display-name");
        String ejbName = extractChildTagAndTrim(element, "ejb-name");

        // get the ejb class.
        String ejbClz = extractChildTagAndTrim(element, "ejb-class");
        if (ejbClz != null)
        {
            ejb = javaClassDao.createJavaClass(ejbClz);
        }

        String sessionType = extractChildTagAndTrim(element, "session-type");
        String transactionType = extractChildTagAndTrim(element, "transaction-type");

        MessageDrivenBeanFacetModel mdb = mdbDao.create();
        mdb.setJavaClassFacet(ejb);
        mdb.setMessageDrivenBeanName(ejbName);
        mdb.setDisplayName(displayName);
        mdb.setEjbId(ejbId);
        mdb.setSessionType(sessionType);
        mdb.setTransactionType(transactionType);

        List<EnvironmentReferenceModel> refs = processEnvironmentReference(element);
        for (EnvironmentReferenceModel ref : refs)
        {
            mdb.addMeta(ref);
        }

        ejbConfig.addMessageDriven(mdb);
        mdbDao.commit();
    }

    protected void processEntityElement(EjbConfigurationFacetModel ejbConfig, Element element)
    {
        JavaClassModel localHome = null;
        JavaClassModel local = null;
        JavaClassModel ejb = null;

        String ejbId = extractAttributeAndTrim(element, "id");
        String displayName = extractChildTagAndTrim(element, "display-name");
        String ejbName = extractChildTagAndTrim(element, "ejb-name");

        // get local class.
        String localClz = extractChildTagAndTrim(element, "local");
        if (localClz != null)
        {
            local = javaClassDao.createJavaClass(localClz);
        }

        // get local home class.
        String localHomeClz = extractChildTagAndTrim(element, "local-home");
        if (localHomeClz != null)
        {
            localHome = javaClassDao.createJavaClass(localHomeClz);
        }

        // get the ejb class.
        String ejbClz = extractChildTagAndTrim(element, "ejb-class");
        if (ejbClz != null)
        {
            ejb = javaClassDao.createJavaClass(ejbClz);
        }

        String persistenceType = extractChildTagAndTrim(element, "persistence-type");

        // create new entity facet.
        EjbEntityFacetModel entity = ejbEntityDao.create();
        entity.setPersistenceType(persistenceType);
        entity.setEjbId(ejbId);
        entity.setDisplayName(displayName);
        entity.setEjbEntityName(ejbName);
        entity.setJavaClassFacet(ejb);
        entity.setEjbLocalHome(localHome);
        entity.setEjbLocal(local);

        List<EnvironmentReferenceModel> refs = processEnvironmentReference(element);
        for (EnvironmentReferenceModel ref : refs)
        {
            entity.addMeta(ref);
        }

        ejbConfig.addEjbEntity(entity);
        ejbEntityDao.commit();
    }

    protected List<EnvironmentReferenceModel> processEnvironmentReference(Element element)
    {
        List<EnvironmentReferenceModel> resources = new LinkedList<EnvironmentReferenceModel>();

        // find JMS references...
        List<Element> queueReferences = $(element).find("resource-env-ref").get();
        for (Element e : queueReferences)
        {
            String type = $(e).child("resource-env-ref-type").text();
            String name = $(e).child("resource-env-ref-name").text();

            type = StringUtils.trim(type);
            name = StringUtils.trim(name);

            EnvironmentReferenceModel ref = envRefDao.createEnvironmentReference(name, type);
            LOG.info("Adding name: " + name + ", type: " + type);
            resources.add(ref);
        }

        return resources;
    }

    protected String extractAttributeAndTrim(Element element, String property)
    {
        String result = $(element).attr(property);
        return StringUtils.trimToNull(result);
    }

    protected String extractChildTagAndTrim(Element element, String property)
    {
        String result = $(element).find(property).first().text();
        return StringUtils.trimToNull(result);
    }
}
