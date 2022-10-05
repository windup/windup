package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.WindupXMLRulesetParsingException;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = MetadataExecuteAfterHandler.EXECUTE_AFTER_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataExecuteAfterHandler implements ElementHandler<String> {

    public static final String EXECUTE_AFTER_ELEMENT = "executeAfter";

    @Override
    public String processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String executeAfterId = element.getTextContent();
        if (executeAfterId == null || executeAfterId.isEmpty()) {
            throw new WindupXMLRulesetParsingException("The '" + EXECUTE_AFTER_ELEMENT + "' must contain non-empty text value");
        }
        return executeAfterId;
    }

}