package org.jboss.windup.engine.visitor.inspector;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.exception.MarshallingException;
import org.jboss.windup.engine.util.xml.XmlUtil;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Adds the MavenFacet to the XML.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class MavenFacetVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(MavenFacetVisitor.class);

    private final Map<String, String> namespaces;

    public MavenFacetVisitor()
    {
        namespaces = new HashMap<String, String>();
        namespaces.put("pom", "http://maven.apache.org/POM/4.0.0");
    }

    @Inject
    private MavenFacetDao mavenDao;

    @Inject
    private XmlResourceDao xmlResourceDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        // visit all XML files that have a maven namespace...
        long total = xmlResourceDao.count(xmlResourceDao.containsNamespaceURI("http://maven.apache.org/POM/4.0.0"));

        int i = 1;
        for (XmlResourceModel entry : xmlResourceDao.containsNamespaceURI("http://maven.apache.org/POM/4.0.0"))
        {
            visitXmlResource(entry);
            i++;
            LOG.info("Processed " + i + " of " + total + " Maven POMs.");
        }
        mavenDao.commit();
    }

    @Override
    public void visitXmlResource(XmlResourceModel entry)
    {
        try
        {
            Document document = entry.asDocument();
            /*
             * String modelVersion = $(document).namespace("pom", "http://maven.apache.org/POM/4.0.0")
             * .xpath("/pom:project/pom:modelVersion").text();
             */

            // modelVersion
            String modelVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:modelVersion", namespaces);
            String name = XmlUtil.xpathExtract(document, "/pom:project/pom:name", namespaces);
            String description = XmlUtil.xpathExtract(document, "/pom:project/pom:description", namespaces);
            String url = XmlUtil.xpathExtract(document, "/pom:project/pom:url", namespaces);

            String groupId = XmlUtil.xpathExtract(document, "/pom:project/pom:groupId", namespaces);
            String artifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:artifactId", namespaces);
            String version = XmlUtil.xpathExtract(document, "/pom:project/pom:version", namespaces);

            String parentGroupId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:groupId", namespaces);
            String parentArtifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:artifactId", namespaces);
            String parentVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:version", namespaces);

            if (StringUtils.isBlank(groupId) && StringUtils.isNotBlank(parentGroupId))
            {
                groupId = parentGroupId;
            }
            if (StringUtils.isBlank(version) && StringUtils.isNotBlank(parentVersion))
            {
                version = parentVersion;
            }

            MavenFacetModel facet = mavenDao.createMaven(groupId, artifactId, version);
            facet.setXmlFacet(entry);

            if (StringUtils.isNotBlank(name))
            {
                facet.setName(StringUtils.trim(name));
            }
            if (StringUtils.isNotBlank(description))
            {
                facet.setDescription(StringUtils.trim(description));
            }
            if (StringUtils.isNotBlank(url))
            {
                facet.setURL(StringUtils.trim(url));
            }
            if (StringUtils.isNotBlank(modelVersion))
            {
                facet.setSpecificationVersion(modelVersion);
            }

            if (StringUtils.isNotBlank(parentGroupId))
            {
                // parent

                parentGroupId = resolveProperty(document, namespaces, parentGroupId, version);
                parentArtifactId = resolveProperty(document, namespaces, parentArtifactId, version);
                parentVersion = resolveProperty(document, namespaces, parentVersion, version);

                MavenFacetModel parent = mavenDao.createMaven(parentGroupId, parentArtifactId, parentVersion);
                facet.setParent(parent);
            }

            NodeList nodes = XmlUtil
                        .xpathNodeList(document, "/pom:project/pom:dependencies/pom:dependency", namespaces);
            for (int i = 0, j = nodes.getLength(); i < j; i++)
            {
                Node node = nodes.item(i);
                String dependencyGroupId = XmlUtil.xpathExtract(node, "./pom:groupId", namespaces);
                String dependencyArtifactId = XmlUtil.xpathExtract(node, "./pom:artifactId", namespaces);
                String dependencyVersionId = XmlUtil.xpathExtract(node, "./pom:version", namespaces);

                dependencyGroupId = resolveProperty(document, namespaces, dependencyGroupId, version);
                dependencyArtifactId = resolveProperty(document, namespaces, dependencyArtifactId, version);
                dependencyVersionId = resolveProperty(document, namespaces, dependencyVersionId, version);

                if (StringUtils.isNotBlank(dependencyGroupId))
                {
                    MavenFacetModel dependency = mavenDao.createMaven(dependencyGroupId, dependencyArtifactId,
                                dependencyVersionId);
                    facet.addDependency(dependency);
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Exception reading document.", e);
        }
    }

    protected String resolveProperty(Document document, Map<String, String> namespaces, String property,
                String projectVersion) throws MarshallingException
    {
        if (StringUtils.startsWith(property, "${"))
        {
            // is property...
            String propertyName = StringUtils.removeStart(property, "${");
            propertyName = StringUtils.removeEnd(propertyName, "}");

            switch (propertyName)
            {
            case "pom.version":
            case "project.version":
                return projectVersion;
            default:
                NodeList nodes = XmlUtil.xpathNodeList(document, "//pom:properties/pom:" + propertyName, namespaces);

                if (nodes.getLength() == 0 || nodes.item(0) == null)
                {
                    LOG.warn("Expected: " + property + " but it wasn't found in the POM.");
                }
                else
                {
                    Node node = nodes.item(0);
                    String value = node.getTextContent();
                    return value;
                }
            }

        }
        return property;
    }
}
