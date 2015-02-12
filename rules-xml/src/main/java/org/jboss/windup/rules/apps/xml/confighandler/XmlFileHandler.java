package org.jboss.windup.rules.apps.xml.confighandler;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.condition.XmlFileResult;
import org.jboss.windup.rules.apps.xml.condition.XmlFileXpath;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Represents an {@link XmlFile} {@link Condition}.
 * 
 * Example:
 * 
 * <pre>
 *  &lt;xmlfile xpath="/w:web-app/w:resource-ref/w:res-auth[text() = 'Container']"&gt;
 *     &lt;namespace prefix="w" uri="http://java.sun.com/xml/ns/javaee"/&gt;
 *  &lt;/xmlfile&gt;
 * </pre>
 * 
 * @author jsightler
 *
 */
@NamespaceElementHandler(elementName = "xmlfile", namespace = "http://windup.jboss.org/v1/xml")
public class XmlFileHandler implements ElementHandler<XmlFile>
{

    @Override
    public XmlFile processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String xpath = $(element).attr("matches");
        String as = $(element).attr("as");
        String publicId = $(element).attr("public-id");
        String resultMatch = $(element).attr("xpathResultMatch");
        if(as == null) {
            as = Iteration.DEFAULT_VARIABLE_LIST_STRING;
        }
        if (StringUtils.isBlank(xpath) && StringUtils.isBlank(publicId))
        {
            throw new WindupException("Error, 'xmlfile' element must have a non-empty 'matches' or public-id attribute");
        }
        String inFile = $(element).attr("in");
        
        Map<String, String> namespaceMappings = new HashMap<>();

        List<Element> children = $(element).children("namespace").get();
        for (Element child : children)
        {
            NamespaceEntry namespaceEntry = handlerManager.processElement(child);
            namespaceMappings.put(namespaceEntry.getPrefix(), namespaceEntry.getNamespaceURI());
        }
        
       
        XmlFileXpath xmlFile = XmlFile.matchesXpath(xpath);
        if(resultMatch !=null) {
            xmlFile.resultMatches(resultMatch);
        }
        xmlFile.andDTDPublicId(publicId);
        for (Map.Entry<String, String> nsMapping : namespaceMappings.entrySet())
        {
            xmlFile.namespace(nsMapping.getKey(), nsMapping.getValue());
        }
        if(inFile !=null) {
            xmlFile.inFile(inFile);
        }
        xmlFile.as(as);
        return (XmlFile)xmlFile;
    }
}
