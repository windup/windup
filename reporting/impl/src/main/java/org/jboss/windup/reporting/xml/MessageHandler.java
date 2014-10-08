package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Parses a "message" element, and returns the contents of the element as a {@link String}.
 * 
 * <pre>
 *  &lt;message&gt;
 *          Longer help contents go here
 *  &lt;/message&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = "message", namespace = "http://windup.jboss.org/v1/xml")
public class MessageHandler implements ElementHandler<String>
{

    @Override
    public String processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String message = $(element).text();
        if (StringUtils.isBlank(message))
        {
            throw new WindupException("Error, 'message' element must not be blank");
        }
        return message;
    }
}
