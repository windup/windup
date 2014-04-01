package org.jboss.windup.decorator.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.xmlunit.XmlDifferenceListener;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.custommonkey.xmlunit.Diff;

/**
 * User: rsearls
 * Date: 6/19/13
 */
public class XmlComparatorDecorator implements MetaDecorator<XmlMetadata>, InitializingBean {
    private static final Log LOG = LogFactory.getLog(XmlComparatorDecorator.class);

    private String xmlControlAsFile;
    private String xmlControlAsString;
    private Document controlDoc;
    private String xpathExp;

    private Effort effort = new UnknownEffort();

    public void setXpath(String xpath){
        this.xpathExp = xpath.trim();
    }

    public void setXmlControlAsFile(String xmlControlAsFile) {
        this.xmlControlAsFile = xmlControlAsFile;
    }


    public void setXmlControlAsString(String xmlControlAsString) {
        this.xmlControlAsString = xmlControlAsString;
    }

    public Effort getEffort() {
        return effort;
    }

    public void setEffort(Effort effort) {
        this.effort = effort;
    }


    @Override
    public void processMeta(XmlMetadata file) {

        if (controlDoc == null){
            // some action and
        } else {
            // real work here
            if (this.xpathExp == null){ // make this an assertion test

            } else {

                Document testDoc = getDocFragment(xpathExp, file);

                if (testDoc != null) {
                    if (isdifferent(testDoc, controlDoc)) {
                        // process for print formating (identify specific)
                        LOG.debug("XML differences found");
                    } else {
                        // remove this from list of things to report.
                        LOG.debug("Remove from list.  XML has default settings");
                        LinkedList < AbstractDecoration > dList =
                            new LinkedList<AbstractDecoration>(file.getDecorations());
                        try {
                            file.getDecorations().remove(dList.removeLast());
                        } catch (NoSuchElementException ne){
                            LOG.debug("Nothing removed. Decoration list is empty.");
                        }
                    }
                }
            }
        }

        return;
    }

    @Override
    public void afterPropertiesSet() {
        LOG.debug("Read control " +
            ((xmlControlAsFile != null)? "file: " + xmlControlAsFile : "xmlControlAsString"));
        InputStream inStream = null;

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            if (xmlControlAsFile != null) {
                inStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(xmlControlAsFile);
                controlDoc = dBuilder.parse(inStream);

            } else if (xmlControlAsString != null){
                controlDoc = dBuilder.parse(new InputSource(new ByteArrayInputStream(
                    xmlControlAsString.getBytes("utf-8"))));
            }

        } catch (Exception e){
            LOG.error("Exception error reading " +
                ((xmlControlAsFile != null)? xmlControlAsFile : "xmlControlAsString"), e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ce){
                // eat msg
            }
        }
    }

    /**
     * Extract an XML element.
     *
     * @param xpathExp
     * @param file
     * @return
     */
    private Document getDocFragment(String xpathExp, XmlMetadata file) {

        Document testDoc = null;

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate(xpathExp, file.getParsedDocument(), XPathConstants.NODE);

            if (node != null) {
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer = transFactory.newTransformer();
                StringWriter buffer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(node),
                    new StreamResult(buffer));
                String testStr = buffer.toString();
                LOG.debug("src string: " + testStr);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                testDoc = dBuilder.parse(new InputSource(
                    new ByteArrayInputStream(testStr.getBytes("utf-8"))));
            }
        } catch (XPathExpressionException xee) {
            LOG.error("Invalid XPath expression: " + xpathExp);
        } catch (Exception e) {
            LOG.error("Error extracting XML content: ", e);
        }

        return testDoc;
    }

    private boolean isdifferent(Document testDoc, Document controlDoc) {
        boolean isdifferent = false;

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setNormalize(true);

        Diff myDiff = new Diff(controlDoc, testDoc);
        DetailedDiff myComparisonController = new DetailedDiff(myDiff);
        DifferenceEngine engine = new DifferenceEngine(myComparisonController);
        XmlDifferenceListener listener = new XmlDifferenceListener();
        ElementNameAndAttributeQualifier myElementQualifier =
            new ElementNameAndAttributeQualifier();
        try { //debug
            engine.compare(controlDoc.getDocumentElement(),
                testDoc.getDocumentElement(), listener, myElementQualifier);
        } catch (NullPointerException ne) {
            LOG.error(ne);
        }

        isdifferent =listener.called();
        return isdifferent;
    }
}

