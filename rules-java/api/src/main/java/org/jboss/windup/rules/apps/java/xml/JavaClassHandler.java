package org.jboss.windup.rules.apps.java.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.condition.JavaClassBuilder;
import org.jboss.windup.rules.apps.java.condition.JavaClassBuilderAt;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.joox.JOOX.$;

/**
 * Represents a {@link JavaClass} {@link Condition}.
 * <p>
 * Example:
 *
 * <pre>
 * &lt;javaclass type="javax.servlet.http.HttpServletRequest"&gt;
 *         &lt;location&gt;METHOD_PARAMETER&lt;/location&gt;
 *         &lt;annotation-list-contion|annotation-literal|annotation-type /&gt;
 * &lt;/javaclass&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = JavaClassHandler.ELEM_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class JavaClassHandler implements ElementHandler<JavaClassBuilderAt> {
    public static final String ELEM_NAME = "javaclass";
    private static final String ATTR_REFERENCES = "references";

    @Override
    public JavaClassBuilderAt processElement(ParserContext handlerManager, Element element)
            throws ConfigurationException {
        String type = $(element).attr(ATTR_REFERENCES);
        String as = $(element).attr("as");
        String from = $(element).attr("from");
        String matchesSource = $(element).attr("matchesSource");
        if (StringUtils.isBlank(type)) {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + ATTR_REFERENCES + "' attribute");
        }

        List<TypeReferenceLocation> locations = new ArrayList<>();
        List<Element> children = $(element).children().get();

        Map<String, AnnotationCondition> conditionMap = new HashMap<>();
        List<AnnotationTypeCondition> additionalAnnotationConditions = new ArrayList<>();
        for (Element child : children) {
            String name = child.getAttribute(AnnotationConditionHandler.NAME);
            switch (child.getNodeName()) {
                case "location":
                    TypeReferenceLocation location = handlerManager.processElement(child);
                    locations.add(location);
                    break;
                case AnnotationTypeConditionHandler.ANNOTATION_TYPE:
                    AnnotationCondition annotationCondition = handlerManager.processElement(child);
                    if (StringUtils.isBlank(name)) {
                        additionalAnnotationConditions.add((AnnotationTypeCondition) annotationCondition);
                    } else {
                        if (conditionMap.containsKey(name))
                            throw new WindupException("Duplicate condition detected on annotation element: " + name);
                        conditionMap.put(name, annotationCondition);
                    }
                    break;
                case AnnotationListConditionHandler.ANNOTATION_LIST_CONDITION:
                    annotationCondition = handlerManager.processElement(child);
                    if (StringUtils.isBlank(name)) {
                        name = "value";
                    }
                    if (conditionMap.containsKey(name))
                        throw new WindupException("Duplicate condition detected on annotation element: " + name);
                    else {
                        conditionMap.put(name, annotationCondition);
                    }
                    break;
                case AnnotationLiteralConditionHandler.ANNOTATION_LITERAL:
                    annotationCondition = handlerManager.processElement(child);
                    if (StringUtils.isBlank(name)) {
                        throw new WindupException("Additional Annotation Condition must be an " +
                                AnnotationTypeConditionHandler.ANNOTATION_TYPE + " condition. Could it be that the '" +
                                AnnotationConditionHandler.NAME + "' property is missing?");
                    } else {
                        if (conditionMap.containsKey(name))
                            throw new WindupException("Duplicate condition detected on annotation element: " + name);
                        conditionMap.put(name, annotationCondition);
                    }
                    break;
            }
        }
        JavaClassBuilder javaClassReferences;
        if (from != null) {
            javaClassReferences = JavaClass.from(from).references(type);
        } else {
            javaClassReferences = JavaClass.references(type);
        }
        if (matchesSource != null) {
            javaClassReferences.matchesSource(matchesSource);
        }

        String namePattern = $(element).attr("in");
        if (!StringUtils.isBlank(namePattern)) {
            javaClassReferences.inType(namePattern);
        }

        JavaClassBuilderAt javaClass = javaClassReferences.at(
                locations.toArray(new TypeReferenceLocation[locations.size()]));

        for (Map.Entry<String, AnnotationCondition> entry : conditionMap.entrySet()) {
            javaClass.annotationMatches(entry.getKey(), entry.getValue());
        }

        for (AnnotationTypeCondition condition : additionalAnnotationConditions) {
            javaClass.annotationMatches(condition);
        }

        if (as != null) {
            javaClass.as(as);
        }
        return javaClass;
    }
}
