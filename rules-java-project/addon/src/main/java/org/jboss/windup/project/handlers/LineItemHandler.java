package org.jboss.windup.project.handlers;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.project.condition.Version;
import org.jboss.windup.project.operation.LineItem;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "lineitem", namespace = "http://windup.jboss.org/v1/xml")
public class LineItemHandler implements ElementHandler<LineItem>
{

    @Override
    public LineItem processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String message = $(element).attr("message");
        if (StringUtils.isBlank(message))
        {
            throw new WindupException(
                        "Error, 'lineItem' element must have a non-empty 'message' attribute");
        }
        LineItem lineItem = LineItem.withMessage(message);
        return lineItem;
    }
}
