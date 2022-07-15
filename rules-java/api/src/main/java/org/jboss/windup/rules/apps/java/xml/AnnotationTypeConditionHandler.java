package org.jboss.windup.rules.apps.java.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Creates an {@link AnnotationTypeCondition} for matching on arrays inside of annotations.
 * <p>
 * Syntax is as follows:
 * <pre>
 *     &lt;annotation-type pattern="class name pattern"&gt;
 *          [... subconditions that will match against annotation elements ...]
 *     &lt;/annotation-type&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = AnnotationTypeConditionHandler.ANNOTATION_TYPE, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class AnnotationTypeConditionHandler extends AnnotationConditionHandler implements ElementHandler<AnnotationTypeCondition> {
    public static final String ANNOTATION_TYPE = "annotation-type";
    private static final String PATTERN = "pattern";

    @Override
    public AnnotationTypeCondition processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String pattern = element.getAttribute(PATTERN);
        if (StringUtils.isBlank(pattern))
            throw new WindupException("Element: " + ANNOTATION_TYPE + " requires a " + PATTERN + " attribute!");

        AnnotationTypeCondition condition = new AnnotationTypeCondition(pattern);

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            switch (child.getNodeName()) {
                case AnnotationTypeConditionHandler.ANNOTATION_TYPE:
                case AnnotationListConditionHandler.ANNOTATION_LIST_CONDITION:
                case AnnotationLiteralConditionHandler.ANNOTATION_LITERAL:
                    String name = child.getAttribute(AnnotationConditionHandler.NAME);
                    AnnotationCondition annotationCondition = handlerManager.processElement(child);
                    condition.addCondition(name, annotationCondition);
                    break;
            }
        }

        return condition;
    }
}
