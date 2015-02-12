package org.jboss.windup.rules.apps.java.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * /**
 * 
 * Represents a {@link TypeReferenceLocation}.
 * 
 * Example:
 * 
 * <pre>
 *   &lt;location&gt;METHOD_PARAMETER&lt;/location&gt;
 * </pre>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "location", namespace = "http://windup.jboss.org/v1/xml")
public class TypeReferenceLocationHandler implements ElementHandler<TypeReferenceLocation>
{

    @Override
    public TypeReferenceLocation processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String location = $(element).text();
        if (StringUtils.isBlank(location))
        {
            throw new WindupException("Error, 'location' element must have non-empty contents");
        }

        return Enum.valueOf(TypeReferenceLocation.class, location.trim());
    }
}
