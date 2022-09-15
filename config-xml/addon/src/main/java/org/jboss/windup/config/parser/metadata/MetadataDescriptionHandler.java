package org.jboss.windup.config.parser.metadata;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

/**
 * Contains simple descriptive text describing this ruleset.
 * <p>
 * For example:
 *
 * <pre>
 *     &lt;description&gt;
 *      This ruleset contains rules for converting objects on the Foo Platform to the much more awesome Baz Enterprise
 *      Platform.
 *     &lt;/description&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = MetadataDescriptionHandler.DESCRIPTION, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataDescriptionHandler implements ElementHandler<String> {

    public static final String DESCRIPTION = "description";

    @Override
    public String processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        return element.getTextContent();
    }

}
