package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.HintText;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
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
        String categoryID = $(element).attr("category-id");

        // Backwards compatibility with old rules
        if (StringUtils.isBlank(categoryID))
            categoryID = $(element).attr("severity");

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
                    messageBuilder.append((String)handlerManager.processElement(child));
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

        String issueDisplayModeString = $(element).attr("show-in");
        if (StringUtils.isNotBlank(issueDisplayModeString))
        {
            IssueDisplayMode issueDisplayMode = IssueDisplayMode.parse(issueDisplayModeString);
            hint.withDisplayMode(issueDisplayMode);
        }

        if (StringUtils.isNotBlank(categoryID))
        {
            IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(handlerManager.getRuleLoaderContext().getContext());
            IssueCategory issueCategory = issueCategoryRegistry.getByID(categoryID);
            hint.withIssueCategory(issueCategory);
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
            switch (child.getNodeName())
            {
                case "link":
                    Link link = handlerManager.processElement(child);
                    hint.with(link);
                    break;
                case "tag":
                    tags.add(child.getTextContent());
                    break;
                case "quickfix":
                    Quickfix quickfix = handlerManager.processElement(child);
                    hint.withQuickfix(quickfix);
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

        String firstLineIndent = null;
        for (int i = 0; i < markdown.length(); i++)
        {
            char currentChar = markdown.charAt(i);
            if (currentChar == '\r' || currentChar == '\n')
            {
                String currentLineString = currentLine.toString();
                if (firstLineIndent == null && !StringUtils.isEmpty(currentLineString))
                {
                    int firstNonWhitespaceIndex = StringUtils.indexOfAnyBut(currentLineString, " \t");
                    if (firstNonWhitespaceIndex != -1)
                        firstLineIndent = currentLineString.substring(0, firstNonWhitespaceIndex);
                }
                if (firstLineIndent != null)
                    currentLineString = StringUtils.removeStart(currentLineString, firstLineIndent);

                markdownSB.append(currentLineString).append(SystemUtils.LINE_SEPARATOR);

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
