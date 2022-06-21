package org.jboss.windup.util;

import org.junit.Assert;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Testing the {@link LocationAwareContentHandler}
 */
public class LocationAwareContentHandlerTest {
    private static final String SIMPLE_DOCTYPE_XML_PATH = "src/test/resources/simple-doctype.xml";

    @Test
    public void doctypeInformationTest() throws IOException, SAXException {
        Document document = LocationAwareXmlReader.readXML(new FileInputStream(new File(SIMPLE_DOCTYPE_XML_PATH)));
        LocationAwareContentHandler.Doctype docType = (LocationAwareContentHandler.Doctype) document
                .getUserData(LocationAwareContentHandler.DOCTYPE_KEY_NAME);
        Assert.assertEquals("http://www.objectweb.org/jonas/dtds/jonas-web-app_3_1.dtd", docType.getSystemId());
        Assert.assertEquals("-//ObjectWeb//DTD JOnAS Web App 3.1//EN", docType.getPublicId());
    }

}
