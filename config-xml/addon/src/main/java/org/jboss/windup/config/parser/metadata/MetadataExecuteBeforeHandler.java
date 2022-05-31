package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.WindupXMLRulesetParsingException;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = MetadataExecuteBeforeHandler.EXECUTE_BEFORE_ELEMENT, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataExecuteBeforeHandler implements ElementHandler<String> {

    public static final String EXECUTE_BEFORE_ELEMENT = "executeBefore";

    @Override
    public String processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String executeBefore = element.getTextContent();
        if (executeBefore == null || executeBefore.isEmpty()) {
            throw new WindupXMLRulesetParsingException("The '" + EXECUTE_BEFORE_ELEMENT + "' must contain non-empty text value");
        }
        return executeBefore;
    }

}