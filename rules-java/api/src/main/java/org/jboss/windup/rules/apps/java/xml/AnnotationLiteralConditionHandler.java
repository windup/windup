package org.jboss.windup.rules.apps.java.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationLiteralCondition;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Creates an {@link AnnotationLiteralCondition} for matching on literal values inside of annotations.
 * <p>
 * Syntax is as follows:
 * <pre>
 *     &lt;annotation-literal pattern="parameterized-pattern"&gt;
 *          [... list of subconditions that will match against this element ...]
 *     &lt;/annotation-literal&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = AnnotationLiteralConditionHandler.ANNOTATION_LITERAL, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class AnnotationLiteralConditionHandler extends AnnotationConditionHandler implements ElementHandler<AnnotationLiteralCondition> {
    public static final String ANNOTATION_LITERAL = "annotation-literal";
    private static final String PATTERN = "pattern";

    @Override
    public AnnotationLiteralCondition processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String pattern = element.getAttribute(PATTERN);
        if (StringUtils.isBlank(pattern))
            throw new WindupException(ANNOTATION_LITERAL + " element requires a " + PATTERN + " attribute!");

        return new AnnotationLiteralCondition(pattern);
    }
}
