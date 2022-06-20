package org.jboss.windup.config.parser.metadata;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

/**
 * Indicates whether or not rules in this provider will override rules from another provider. The default value is false.
 * <p>
 * For example:
 *
 * <pre>
 *     &lt;overrideProvider&gt;
 *          true
 *     &lt;/overrideProvider&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = MetadataProviderOverrideHandler.OVERRIDE_PROVIDER, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataProviderOverrideHandler implements ElementHandler<Boolean> {
    public static final String OVERRIDE_PROVIDER = "overrideRules";

    @Override
    public Boolean processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String textContent = element.getTextContent();
        return StringUtils.isNotBlank(textContent) && Boolean.parseBoolean(textContent.trim());
    }

}
