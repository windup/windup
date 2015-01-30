package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;
import java.io.IOException;
import java.util.jar.Attributes;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.filters.StringInputStream;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses response like this:
 *  (e.g. https://repository.jboss.org/nexus/service/local/identify/sha1/0001a7506efdbb48e409cbd55f6498254a108ed5 )
 *
 * <pre>{@code
        <org.sonatype.nexus.rest.model.NexusArtifact>
            <resourceURI>https://repository.jboss.org/nexus/service/local/repositories/central/content/org/jboss/resteasy/resteasy-yaml-provider/2.3.4.Final/resteasy-yaml-provider-2.3.4.Final.jar</resourceURI>
            <groupId>org.jboss.resteasy</groupId>
            <artifactId>resteasy-yaml-provider</artifactId>
            <version>2.3.4.Final</version>
            <packaging>jar</packaging>
            <extension>jar</extension>
            <repoId>central</repoId>
            <contextId>central-ctx</contextId>
            <pomLink>https://repository.jboss.org/nexus/service/local/artifact/maven/redirect?r=central&amp;g=org.jboss.resteasy&amp;a=resteasy-yaml-provider&amp;v=2.3.4.Final&amp;e=pom</pomLink>
            <artifactLink>https://repository.jboss.org/nexus/service/local/artifact/maven/redirect?r=central&amp;g=org.jboss.resteasy&amp;a=resteasy-yaml-provider&amp;v=2.3.4.Final&amp;e=jar</artifactLink>
       </org.sonatype.nexus.rest.model.NexusArtifact>
      }
   <pre>
 *
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class NexusIdentifyServiceIdentifier extends HttpBaseIdentifier {


    /**
     * @param baseURL  Base URL of the endpoint, in which the ${SHA1} token will be replaced with the hash.
     * E.g. https://repository.jboss.org/nexus/service/local/identify/sha1/${SHA1}
     *      https://repository.sonatype.org/service/local/identify/sha1/${SHA1}
     * or   http://search.maven.org/solrsearch/select?q=1:"${SHA1}"&rows=20&wt=json
     * Try with 35379fb6526fd019f331542b4e9ae2e566c57933
     */
    public NexusIdentifyServiceIdentifier(String baseURL)
    {
        super(baseURL);
    }


    @Override
    public GAV parseResponse(String contentType, String responseBody)
    {
        if (!"text/xml".equals(contentType)){
            throw new IllegalArgumentException(this.getClass().getName() + " can only parse XML responses, not: " + contentType);
        }

        return this.parseXMLwithSAX(responseBody);
    }



    private GAV parseXMLwithSAX(String responseBody)
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            NexusIdentifyResponseHandler nexusIdentifyResponseHandler = new NexusIdentifyResponseHandler();

            saxParser.parse(new StringInputStream(responseBody), nexusIdentifyResponseHandler);

            GAV gav = nexusIdentifyResponseHandler.getGav();
            if (null == gav)  // Should not happen.
                throw new IllegalArgumentException("No G:A:V info found.");
            return gav;
        }
        catch (ParserConfigurationException | SAXException | IOException | IllegalArgumentException ex) {
           throw new RuntimeException("Failed parsing the XML response:\n\t" + ex.getMessage(), ex);
        }
    }

}


class NexusIdentifyResponseHandler extends DefaultHandler
{
    String lastElementStart = null;
    GAV gav;
    String g, a , v, c, sha1;


    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException
    {
        lastElementStart = localName;
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        if ("groupId".equals(lastElementStart))
            this.g = new String(ch, start, length).trim();
        else if ("artifactId".equals(lastElementStart))
            this.a = new String(ch, start, length).trim();
        else if ("version".equals(lastElementStart))
            this.v = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (ROOT_ELEMENT_NAME == localName)
            this.gav = new GAV(g, a, v);
    }
    private static final String ROOT_ELEMENT_NAME = "org.sonatype.nexus.rest.model.NexusArtifact";

    @Override
    public void endDocument() throws SAXException
    {
        if (this.gav == null)
            throw new IllegalArgumentException("No G:A:V info found - could not find <" + ROOT_ELEMENT_NAME + "> in the given XML.");
    }





    public GAV getGav()
    {
        return gav;
    }
}