package org.jboss.windup.rules.apps.xml.confighandler;

import static org.joox.JOOX.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.condition.XmlFileFrom;
import org.jboss.windup.rules.apps.xml.condition.XmlFileXpath;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Represents an {@link XmlFile} {@link Condition}.
 * <p>
 * Example:
 *
 * <pre>
 *  &lt;xmlfile xpath="/w:web-app/w:resource-ref/w:res-auth[text() = 'Container']"&gt;
 *     &lt;namespace prefix="w" uri="http://java.sun.com/xml/ns/javaee"/&gt;
 *  &lt;/xmlfile&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "xmlfile", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class XmlFileHandler implements ElementHandler<XmlFile> {

    @Override
    public XmlFile processElement(ParserContext handlerManager, Element element)
            throws ConfigurationException {
        String xpath = $(element).attr("matches");
        String as = $(element).attr("as");
        String from = $(element).attr("from");
        String publicId = $(element).attr("public-id");
        String systemId = $(element).attr("system-id");
        String resultMatch = $(element).attr("xpathResultMatch");
        if (as == null) {
            as = Iteration.DEFAULT_VARIABLE_LIST_STRING;
        }
        if (StringUtils.isBlank(xpath) && StringUtils.isBlank(publicId) && StringUtils.isBlank(systemId)) {
            throw new WindupException("Error, 'xmlfile' element must have a non-empty 'matches', 'public-id' or 'system-id' attribute");
        }
        String inFile = $(element).attr("in");

        Map<String, String> namespaceMappings = new HashMap<>();

        List<Element> children = $(element).children("namespace").get();
        for (Element child : children) {
            NamespaceEntry namespaceEntry = handlerManager.processElement(child);
            namespaceMappings.put(namespaceEntry.getPrefix(), namespaceEntry.getNamespaceURI());
        }

        XmlFileXpath xmlFile;
        if (StringUtils.isNotBlank(from)) {
            XmlFileFrom xmlFileFrom = XmlFile.from(from);
            xmlFile = xmlFileFrom.matchesXpath(xpath);
        } else {
            xmlFile = XmlFile.matchesXpath(xpath);
        }

        if (resultMatch != null) {
            xmlFile.resultMatches(resultMatch);
        }
        xmlFile.andDTDPublicId(publicId);
        xmlFile.andDTDSystemId(systemId);
        for (Map.Entry<String, String> nsMapping : namespaceMappings.entrySet()) {
            xmlFile.namespace(nsMapping.getKey(), nsMapping.getValue());
        }
        if (inFile != null) {
            xmlFile.inFile(inFile);
        }
        xmlFile.as(as);
        return (XmlFile) xmlFile;
    }
}
