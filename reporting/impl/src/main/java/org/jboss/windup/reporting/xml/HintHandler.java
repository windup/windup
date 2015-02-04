package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.HintText;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Classification} operation to the currently selected items.
 * 
 * Expected format:
 * 
 * <pre>
 * &lt;hint message="hint" effort="8"&gt;
 * &lt;/hint&gt;
 * </pre>
 * 
 * Alternatively, the hint can be in its own element. This is primary useful for longer hint content:
 * 
 * <pre>
 * &lt;hint title="Short description" effort="8"&gt;
 *  &lt;message&gt;
 *          Longer help contents go here (markdown format is supported)
 *  &lt;/message&gt;
 * &lt;/hint&gt;
 * </pre>
 * 
 * Also note that markdown formatting is fully supported via the <a href="http://www.pegdown.org/">Pegdown</a> library.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "hint", namespace = "http://windup.jboss.org/v1/xml")
public class HintHandler implements ElementHandler<Hint>
{

    @Override
    public Hint processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String title = $(element).attr("title");
        String message = $(element).attr("message");
        String in = $(element).attr("in");

        if (StringUtils.isBlank(message))
        {
            StringBuilder messageBuilder = new StringBuilder();
            List<Element> children = $(element).children().get();
            for (Element child : children)
            {
                if (child.getNodeName().equals("message"))
                {
                    messageBuilder.append(handlerManager.processElement(child));
                }
            }
            message = messageBuilder.toString();
            // remove the leading spaces as these can mess with markdown formatting
            message = trimLeadingAndTrailingSpaces(message);
        }

        if (StringUtils.isBlank(message))
        {
            throw new WindupException("Error, 'hint' element must have a non-empty 'message' attribute or element");
        }

        String effortStr = $(element).attr("effort");

        HintText hint;
        if (!StringUtils.isBlank(title))
        {
            hint = Hint.in(in).titled(title).withText(message);
        }
        else
        {
            hint = Hint.in(in).withText(message);
        }
        if (!StringUtils.isBlank(effortStr))
        {
            try
            {
                int effort = Integer.parseInt(effortStr);
                hint.withEffort(effort);
            }
            catch (NumberFormatException e)
            {
                throw new WindupException("Could not parse effort level: " + effortStr + " as an integer!");
            }
        }

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            if (child.getNodeName().equals("link"))
            {
                Link link = handlerManager.processElement(child);
                hint.with(link);
            }
        }
        return (Hint) hint;
    }

    private String trimLeadingAndTrailingSpaces(String markdown)
    {
        StringBuilder markdownSB = new StringBuilder();

        StringBuilder currentLine = new StringBuilder();
        for (int i = 0; i < markdown.length(); i++)
        {
            char currentChar = markdown.charAt(i);
            if (currentChar == '\r' || currentChar == '\n')
            {
                markdownSB.append(currentLine.toString().trim()).append(SystemUtils.LINE_SEPARATOR);
                currentLine.setLength(0);

                // skip the next line separator for \r\n cases
                if (currentChar == '\r' && markdown.length() > (i + 1) && markdown.charAt(i + 1) == '\n')
                {
                    i++;
                }
            }
            else
            {
                currentLine.append(currentChar);
            }
        }
        markdownSB.append(currentLine);

        return markdownSB.toString();
    }
}
