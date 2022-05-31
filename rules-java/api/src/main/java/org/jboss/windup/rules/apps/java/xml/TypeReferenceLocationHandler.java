package org.jboss.windup.rules.apps.java.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * /**
 * <p>
 * Represents a {@link TypeReferenceLocation}.
 * <p>
 * Example:
 *
 * <pre>
 *   &lt;location&gt;METHOD_PARAMETER&lt;/location&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "location", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TypeReferenceLocationHandler implements ElementHandler<TypeReferenceLocation> {
    @Override
    public TypeReferenceLocation processElement(ParserContext handlerManager, Element element)
            throws ConfigurationException {
        String location = $(element).text();
        if (StringUtils.isBlank(location)) {
            throw new WindupException("Error, 'location' element must have non-empty contents");
        }

        return Enum.valueOf(TypeReferenceLocation.class, location.trim());
    }
}
