package org.jboss.windup.reporting.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.TechnologyTag;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = TechnologyTagHandler.TECHNOLOGY_TAG, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class TechnologyTagHandler implements ElementHandler<Object> {
    public static final String TECHNOLOGY_TAG = "technology-tag";
    private static final String LEVEL = "level";

    @Override
    public TechnologyTag processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String tag = element.getTextContent();
        if (StringUtils.isNotBlank(tag)) {
            tag = tag.trim();
        } else {
            throw new WindupException("Error, '" + TechnologyTagHandler.TECHNOLOGY_TAG + "' element must have non-empty content");
        }
        TechnologyTagLevel issueCategory = TechnologyTagLevel.INFORMATIONAL;
        String category = element.getAttribute(TechnologyTagHandler.LEVEL);
        if (StringUtils.isNotBlank(category)) {
            issueCategory = TechnologyTagLevel.valueOf(category);
        }
        return TechnologyTag.withName(tag).withTechnologyTagLevel(issueCategory);
    }
}
