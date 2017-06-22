package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.LineitemExists;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Creates a {@link LineitemExists} that searches for the given lineitem message. Example usage:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *             &lt;not&gt;
 *                 &lt;lineitem-exists message="Use log4j module" /&gt;
 *             &lt;/not&gt;
 *         &lt;/when&gt;
 *         &lt;perform&gt;
 *             [...]
 *         &lt;/perform&gt;
 *     &lt;/rule&gt;
 * </pre>
 * 
 * @author mnovotny
 *
 */
@NamespaceElementHandler(elementName = LineitemExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class LineitemExistsHandler implements ElementHandler<LineitemExists>
{
    static final String ELEMENT_NAME = "lineitem-exists";
    private static final String MESSAGE = "message";

    @Override
    public LineitemExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String messagePattern = $(element).attr(MESSAGE);

        if (StringUtils.isBlank(messagePattern))
        {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + MESSAGE + "' attribute");
        }

        return LineitemExists.withMessage(messagePattern);
    }
}
