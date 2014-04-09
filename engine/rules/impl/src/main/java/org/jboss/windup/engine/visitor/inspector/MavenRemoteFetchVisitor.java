package org.jboss.windup.engine.visitor.inspector;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.xml.XmlUtil;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * If the JAR did not contain a POM, try and look up the POM from Maven Central.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class MavenRemoteFetchVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(MavenRemoteFetchVisitor.class);

    private final Map<String, String> namespaces;

    public MavenRemoteFetchVisitor()
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
        return VisitorPhase.COMPOSITION;
    }

    @Override
    public void run()
    {
        // visit all XML files that have a maven namespace...
        for (XmlResource entry : xmlResourceDao.containsNamespaceURI("http://maven.apache.org/POM/4.0.0"))
        {
            visitXmlResource(entry);
        }
        mavenDao.commit();
    }

    @Override
    public void visitXmlResource(XmlResource entry)
    {
        LOG.info("Resource: " + entry.getResource().asVertex());
        try
        {
            Document document = xmlResourceDao.asDocument(entry);
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

            final String parentGroupId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:groupId",
                        namespaces);
            final String parentArtifactId = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:artifactId",
                        namespaces);
            final String parentVersion = XmlUtil.xpathExtract(document, "/pom:project/pom:parent/pom:version",
                        namespaces);

            if (StringUtils.isBlank(groupId) && StringUtils.isNotBlank(parentGroupId))
            {
                groupId = parentGroupId;
            }
            if (StringUtils.isBlank(version) && StringUtils.isNotBlank(parentVersion))
            {
                version = parentVersion;
            }

            MavenFacet facet = mavenDao.createMaven(groupId, artifactId, version);
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
                MavenFacet parent = mavenDao.createMaven(parentGroupId, parentArtifactId, parentVersion);
                facet.setParent(parent);
            }

            NodeList nodes = XmlUtil
                        .xpathNodeList(document, "/pom:project/pom:dependencies/pom:dependency", namespaces);
            for (int i = 0, j = nodes.getLength(); i < j; i++)
            {
                Node node = nodes.item(i);
                final String dependencyGroupId = XmlUtil.xpathExtract(node, "./pom:groupId", namespaces);
                final String dependencyArtifactId = XmlUtil.xpathExtract(node, "./pom:artifactId", namespaces);
                final String dependencyVersionId = XmlUtil.xpathExtract(node, "./pom:version", namespaces);

                if (StringUtils.isNotBlank(dependencyGroupId))
                {
                    MavenFacet dependency = mavenDao.createMaven(dependencyGroupId, dependencyArtifactId,
                                dependencyVersionId);
                    facet.addDependency(dependency);
                }
            }
            LOG.info("Successfully read XML..");
        }
        catch (Exception e)
        {
            LOG.error("Exception reading document.", e);
        }
    }

}
