package org.jboss.windup.reporting.integration.forge;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.windup.metadata.type.FileMetadata;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Generate new bean interface and implementation object based upon contents
 * of XML.
 *
 * User: rsearls
 * Date: 8/23/13
 */
public class XmlFileProcessor implements FileProcessor {
    private static final Log LOG = LogFactory.getLog(XmlFileProcessor.class);

    private SwitchyardForgeRegistrar switchyardForgeRegistrar;

    public XmlFileProcessor(SwitchyardForgeRegistrar switchyardForgeRegistrar){

        if (switchyardForgeRegistrar == null){
            LOG.error("switchyardForgeRegistrar can not be null.");
            throw new IllegalArgumentException("switchyardForgeRegistrar can not be null.");
        }

        this.switchyardForgeRegistrar = switchyardForgeRegistrar;
    }

    @Override
    public void process(FileMetadata entry, String archiveName){

        if (archiveName == null || archiveName.trim().length() == 0){
            LOG.error("archiveName can not be null or blank.");
            throw new IllegalArgumentException("archiveName can not be null or blank.");
        }


        Document destDoc = parseXmlDoc(entry);
        if (destDoc == null) {
            try {
                LOG.warn("No Document created from file: " +
                    entry.getFilePointer().getCanonicalPath());
            } catch (IOException ie) {
                LOG.warn(ie);
            }
            return;
        }

        Project project = switchyardForgeRegistrar.getProject();
        if (project == null) {
            LOG.warn("No project found for archive named: " + archiveName);
        } else {

            MetadataFacet mdf = project.getFacet(MetadataFacet.class);
            mdf.setProjectName(archiveName);

            FacetFactory facetFactory = switchyardForgeRegistrar.getFacetFactory();
            facetFactory.install(project, SwitchYardFacet.class);

            try {
                XPath xpath = XPathFactory.newInstance().newXPath();
                String exp = "/jbossesb/services/service/actions/action";
                NodeList nList = (NodeList) xpath.evaluate(exp, destDoc, XPathConstants.NODESET);
                if (nList != null) {
                    for (int i = 0; i < nList.getLength(); i++) {
                        Node n = nList.item(i);
                        NamedNodeMap attr = n.getAttributes();
                        Node nClass = attr.getNamedItem("class");
                        if (nClass == null) {
                            Node nName = attr.getNamedItem("name");
                            if (nName == null) {
                                LOG.warn("action tag with missing name and class attribute.");
                            } else {
                                LOG.warn("No class attribute for action tag class="
                                    + nName.getNodeValue());
                            }

                        } else {
                            String className = StringUtils.substringAfterLast(
                                nClass.getNodeValue(), ".");
                            String packageName = StringUtils.substringBeforeLast(
                                nClass.getNodeValue(), ".");

                            LOG.debug("Action tag's   baseName: " + className
                                + "   package: " + packageName);

                            BeanFacet beanFacet = facetFactory.create(project, BeanFacet.class);
                            beanFacet.install();
                            BeanServiceConfigurator beanServiceConfigurator =
                                new BeanServiceConfigurator();
                            beanServiceConfigurator.newBean(project, packageName, className);

                            switchyardForgeRegistrar.getSwitchYardConfigurator().createServiceTest(
                                project, className, packageName);
                        }
                    }
                }

            } catch (XPathExpressionException e) {
                LOG.warn(e);
            } catch (Exception ex) {
                LOG.warn(ex);
            }
        }

    }

    private Document parseXmlDoc(FileMetadata entry){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( false );
        String feat = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

        Document destDoc = null;
        try {
            dbf.setFeature( feat, false );
            DocumentBuilder db = dbf.newDocumentBuilder();
            destDoc = db.parse(entry.getFilePointer());
        } catch( ParserConfigurationException ex) {
            LOG.warn( "Couldn't set " + feat + " to false. The parser may attempt to load DTD." );
        } catch(SAXException se){
            LOG.warn(se);
        } catch(IOException ie){
            LOG.warn(ie);
        }
        return destDoc;
    }
}
