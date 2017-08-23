package org.jboss.windup.reporting.xml;

import java.util.HashSet;
import static org.joox.JOOX.$;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
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
 * &lt;classification; classification="classification text" description="description of classification" effort="8"&gt;
 *  &lt;link href="http://www.foo.com/" description="Helpful text" /&gt;
 * &lt;/classification&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:dynawest@gmail.com">Ondrej Zizka</a>
 */
@NamespaceElementHandler(elementName = "classification", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class ClassificationHandler implements ElementHandler<Classification>
{
    private static final Logger LOG = Logger.getLogger(ClassificationHandler.class.getName());

    @Override
    public Classification processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String classificationStr = $(element).attr("title");
        if (StringUtils.isBlank(classificationStr))
        {
            throw new WindupException(
                        "Error, 'classification' element must have a non-empty 'title' attribute (eg, 'Decompiled Source File')");
        }
        String of = $(element).attr("of");
        String effortStr = $(element).attr("effort");
        String issueCategoryID = $(element).attr("category-id");

        // Backwards compatibility with old rules
        if (StringUtils.isBlank(issueCategoryID))
            issueCategoryID = $(element).attr("severity");

        Set<String> tags = new HashSet<>();

        Classification classification = (Classification) Classification.as(classificationStr);
        if (of != null)
        {
            classification.setVariableName(of);
        }
        if (StringUtils.isNotBlank(effortStr))
        {
            try
            {
                int effort = Integer.parseInt(effortStr);
                classification.withEffort(effort);
            }
            catch (NumberFormatException e)
            {
                throw new WindupException("Could not parse effort level: " + effortStr + " as an integer!");
            }
        }

        if (StringUtils.isNotBlank(issueCategoryID))
        {
            IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(handlerManager.getRuleLoaderContext().getContext());
            IssueCategory issueCategory = issueCategoryRegistry.getByID(issueCategoryID);
            classification.withIssueCategory(issueCategory);
        }

        String issueDisplayModeString = $(element).attr("issue-display-mode");
        if (StringUtils.isNotBlank(issueDisplayModeString))
        {
            IssueDisplayMode issueDisplayMode = IssueDisplayMode.parse(issueDisplayModeString);
            if (issueDisplayMode == IssueDisplayMode.DETAIL_ONLY && classification.getEffort() != 0)
                LOG.warning("WARNING: classification: " + classificationStr + " with effort " + effortStr + " is marked as detail only. This is generally a mistake.");
            classification.withIssueDisplayMode(issueDisplayMode);
        }

        String description = $(element).child("description").text();
        if (StringUtils.isNotBlank(description))
        {
            description = HintHandler.trimLeadingAndTrailingSpaces(description);
            classification.withDescription(description);
        }

        List<Element> children = $(element).children("link").get();
        for (Element child : children)
        {
            Link link = handlerManager.processElement(child);
            classification.with(link);
        }

        children = $(element).children("tag").get();
        for (Element child : children)
        {
            tags.add(child.getTextContent());
        }
        classification.withTags(tags);
        
        // Quickfix parsing 
        children = $(element).children("quickfix").get();
        for (Element child : children)
        {
            Quickfix quickfix = handlerManager.processElement(child);
            classification.withQuickfix(quickfix);
        }

        return classification;
    }
}
