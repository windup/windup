package org.jboss.windup.reporting.xml;

import java.util.HashSet;
import static org.joox.JOOX.$;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.HintText;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Classification} operation to the currently selected items.
 *
 * Expected format:
 *
 * <pre>
 * &lt;hint message="hint" effort="8" severity="INFO"&gt;
 * &lt;/hint&gt;
 * </pre>
 *
 * Alternatively, the hint message can be in its own element. This is primary useful for longer hint content:
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
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = "hint", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class HintHandler implements ElementHandler<Hint>
{

    @Override
    public Hint processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String title = $(element).attr("title");
        String severityStr = $(element).attr("severity");
        String message = $(element).attr("message");
        String in = $(element).attr("in");
        Set<String> tags = new HashSet<>();

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

        if (StringUtils.isNotBlank(severityStr))
        {
            Severity severity = Severity.valueOf(severityStr.toUpperCase());
            hint.withSeverity(severity);
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
            switch(child.getNodeName()){
                case "link":
                    Link link = handlerManager.processElement(child);
                    hint.with(link);
                    break;
                case "tag":
                    tags.add(child.getTextContent());
                    break;
            }
        }

        hint.withTags(tags);

        return (Hint) hint;
    }

    public static String trimLeadingAndTrailingSpaces(String markdown)
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
