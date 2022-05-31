package org.jboss.windup.rules.apps.xml.xml;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * This implements a SAX handler that maintains a list of validation errors for use by {@link ValidateXmlFilesRuleProvider}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
class ValidateXmlHandler extends DefaultHandler2 {
    private static final String XMLSCHEMA_ATTRIBUTE_NAME = "schemaLocation";
    private static final String NO_NAMESPACE_XMLSCHEMA_ATTRIBUTE_NAME = "noNamespaceSchemaLocation";

    private final List<SAXParseException> parseExceptions = new ArrayList<>();
    private final List<String> xsdURLs = new ArrayList<>();
    private final EnhancedEntityResolver2 entityResolver;
    private boolean firstElementFound = false;
    private Locator locator;
    private boolean onlineMode;

    public ValidateXmlHandler(boolean onlineMode) {
        this.onlineMode = onlineMode;
        this.entityResolver = new EnhancedEntityResolver2(onlineMode);
    }

    static boolean isNetworkUrl(String url) {
        return url != null && !url.startsWith("file");
    }

    /**
     * Contains a list of XSD urls found in this document.
     */
    public List<String> getXsdURLs() {
        return xsdURLs;
    }

    /**
     * Contains a list of parse failures in the document.
     */
    public List<SAXParseException> getParseExceptions() {
        return parseExceptions;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return entityResolver.resolveEntity(publicId, systemId);
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        return entityResolver.resolveEntity(name, publicId, baseURI, systemId);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!firstElementFound) {
            firstElementFound = true;
            try {
                xsdURLs.addAll(getXSDLocations(attributes));

                // validate the xsd urls
                for (String xsdUrl : xsdURLs) {
                    try {
                        if (!onlineMode && isNetworkUrl(xsdUrl)) {
                            continue;
                        }

                        URLConnection urlConnection = new URL(xsdUrl).openConnection();
                        urlConnection.setConnectTimeout(10000);
                        urlConnection.setReadTimeout(10000);
                        // this will throw if invalid
                        InputStream inputStream = urlConnection.getInputStream();
                    } catch (IOException e) {
                        parseExceptions.add(new SAXParseException(e.getMessage(), locator, e));
                    }
                }
            } catch (InvalidXSDURLException e) {
                parseExceptions.add(new SAXParseException(e.getMessage(), locator, e));
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        super.warning(e);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        parseExceptions.add(e);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        parseExceptions.add(e);
    }

    private List<String> getXSDLocations(Attributes attributes) throws InvalidXSDURLException {
        List<String> xsdUrls = new ArrayList<>();
        if (attributes == null) {
            return xsdUrls;
        }

        String xsdSchemaAttr = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                XMLSCHEMA_ATTRIBUTE_NAME);
        if (StringUtils.isNotBlank(xsdSchemaAttr)) {
            String[] splittedXslSchemaAttr = xsdSchemaAttr.split("\\s");
            for (int i = 1; i < splittedXslSchemaAttr.length; i += 2) {
                String urlStr = splittedXslSchemaAttr[i];
                xsdUrls.add(urlStr);
            }
        }

        String noNamespaceSchema = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                NO_NAMESPACE_XMLSCHEMA_ATTRIBUTE_NAME);
        if (StringUtils.isNotBlank(noNamespaceSchema)) {
            xsdUrls.add(noNamespaceSchema);
        }
        return xsdUrls;
    }
}
