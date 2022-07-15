package org.jboss.windup.rules.apps.java.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationListCondition;
import org.w3c.dom.Element;

import java.util.List;

import static org.joox.JOOX.$;

/**
 * Creates an {@link AnnotationListCondition} for matching on arrays inside of annotations.
 * <p>
 * Syntax is as follows:
 * <pre>
 *     &lt;annotation-list-condition index="[optional attribute specified an array index]"&gt;
 *          [... list of subconditions that will match against this element ...]
 *     &lt;/annotation-list-condition&gt;
 * </pre>
 * <p>
 * If the index is not specified, the condition will be applied to every item in the array. If the conditions apply
 * to any element in the array, then the condition will be matched.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = AnnotationListConditionHandler.ANNOTATION_LIST_CONDITION, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class AnnotationListConditionHandler extends AnnotationConditionHandler implements ElementHandler<AnnotationCondition> {
    public static final String ANNOTATION_LIST_CONDITION = "annotation-list";
    private static final String INDEX = "index";

    @Override
    public AnnotationCondition processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String indexStr = element.getAttribute(INDEX);

        AnnotationListCondition condition;
        if (StringUtils.isNotBlank(indexStr))
            condition = new AnnotationListCondition(Integer.parseInt(indexStr));
        else
            condition = new AnnotationListCondition();

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            switch (child.getNodeName()) {
                case AnnotationTypeConditionHandler.ANNOTATION_TYPE:
                case AnnotationListConditionHandler.ANNOTATION_LIST_CONDITION:
                case AnnotationLiteralConditionHandler.ANNOTATION_LITERAL:
                    AnnotationCondition annotationCondition = handlerManager.processElement(child);
                    condition.addCondition(annotationCondition);
                    break;
            }
        }

        return condition;
    }
}
