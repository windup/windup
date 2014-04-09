package org.jboss.windup.engine.visitor.inspector;

import static org.joox.JOOX.$;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.xml.XmlUtil;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.dao.HibernateEntityDao;
import org.jboss.windup.graph.dao.HibernateMappingDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.HibernateMappingFacet;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Goes over all XML files that contain Hibernate namespace and checks root tag, then adds Hibernate facet.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class HibernateMappingVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(HibernateMappingVisitor.class);

    private static final String hibernateRegex = "(?i).*hibernate.mapping.*";

    @Inject
    private DoctypeDao doctypeDao;

    @Inject
    private HibernateEntityDao hibernateEntityDao;

    @Inject
    private HibernateMappingDao hibernateMappingDao;

    @Inject
    private JavaClassDao javaClassDao;

    @Inject
    private XmlResourceDao xmlResourceDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.COMPOSITION;
    }

    @Override
    public void run()
    {
        // visit all Doctypes matching Hibernate in system or public ids.
        long total = doctypeDao.count(doctypeDao.findSystemIdOrPublicIdMatchingRegex(hibernateRegex));

        int i = 1;
        for (DoctypeMeta doctype : doctypeDao.findSystemIdOrPublicIdMatchingRegex(hibernateRegex))
        {
            i++;
            LOG.info("Processed " + i + " of " + " Doctypes.");
            visitDoctype(doctype);
        }

        javaClassDao.commit();
    }

    @Override
    public void visitDoctype(DoctypeMeta entry)
    {
        LOG.info("Doctype: ");
        LOG.info("  - publicId [" + entry.getPublicId() + "]");
        LOG.info("  - systemId [" + entry.getSystemId() + "]");

        String publicId = entry.getPublicId();
        String systemId = entry.getSystemId();

        // extract the version information from the public / system ID.
        String versionInformation = extractVersion(publicId, systemId);

        int batch = 0;
        for (XmlResource xml : entry.getXmlResources())
        {

            // create a facet, and then identify the XML.
            HibernateMappingFacet hibernateMapping = hibernateMappingDao.create();
            hibernateMapping.setXmlFacet(xml);

            Document doc = xmlResourceDao.asDocument(xml);

            if (!XmlUtil.xpathExists(doc, "/hibernate-mapping", null))
            {
                LOG.warn("Docment does not contain Hibernate Mapping.");
                continue;
            }

            String clzPkg = $(doc).xpath("/hibernate-mapping").attr("package");
            String clzName = $(doc).xpath("/hibernate-mapping/class").attr("name");
            String tableName = $(doc).xpath("/hibernate-mapping/class").attr("table");
            String schemaName = $(doc).xpath("/hibernate-mapping/class").attr("schema");
            String catalogName = $(doc).xpath("/hibernate-mapping/class").attr("catalog");

            if (StringUtils.isBlank(clzName))
            {
                LOG.debug("Docment does not contain class name. Skipping.");
                continue;
            }

            // prepend with package name.
            if (StringUtils.isNotBlank(clzPkg) && !StringUtils.startsWith(clzName, clzPkg))
            {
                clzName = clzPkg + "." + clzName;
            }

            // get a reference to the Java class.
            JavaClass clz = javaClassDao.getJavaClass(clzName);

            // create the hibernate facet.
            HibernateEntityFacet hibernateEntity = hibernateEntityDao.create();
            hibernateEntity.setSpecificationVersion(versionInformation);
            hibernateEntity.setJavaClassFacet(clz);
            hibernateEntity.setTableName(tableName);
            hibernateEntity.setSchemaName(schemaName);
            hibernateEntity.setCatalogName(catalogName);

            // map the entity back to the XML mapping.
            hibernateMapping.setHibernateEntity(hibernateEntity);

            if (StringUtils.isNotBlank(versionInformation))
            {
                hibernateEntity.setSpecificationVersion(versionInformation);
                hibernateMapping.setSpecificationVersion(versionInformation);
            }

            if (batch % 100 == 0 && batch > 0)
            {
                javaClassDao.commit();
            }
            batch++;

        }
    }

    protected String extractVersion(String publicId, String systemId)
    {
        Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z.-]+");

        if (StringUtils.isNotBlank(publicId))
        {
            Matcher matcher = pattern.matcher(publicId);
            if (matcher.find())
            {
                return matcher.group();
            }
        }

        if (StringUtils.isNotBlank(systemId))
        {
            Matcher matcher = pattern.matcher(systemId);
            if (matcher.find())
            {
                String match = matcher.group();

                // for system ID, make sure to remove the ".dtd" that could come in.
                return StringUtils.removeEnd(match, ".dtd");
            }
        }

        return null;
    }
}
