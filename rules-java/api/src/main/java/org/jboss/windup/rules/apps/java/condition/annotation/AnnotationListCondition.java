package org.jboss.windup.rules.apps.java.condition.annotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.google.common.collect.Iterables;

/**
 * <p>
 * Matches on an item in an array within an annotation.
 * </p>
 *
 * <p>
 * For example, given the following annotation:
 * </p>
 *
 * <pre>
 * @Foo(value = { 1, 2, 3, 4 })
 * </pre>
 *
 * <p>
 * This could be created with an index and a literal condition like this:
 *
 * <pre>
 *     new AnnotationListCondition(1).addCondition(new AnnotationLiteralCondition("2"));
 * </pre>
 * </p>
 *
 * <p>
 * If the "index" parameter is not specified, then all array entries will be searched. As long as at least one matches,
 * then the condition will return true. This is useful in cases where the search is for a particular item that may
 * occur at any point within an array.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationListCondition extends AnnotationCondition {
    private static final int ANY = -1;

    private final int index;

    private List<AnnotationCondition> conditions = new ArrayList<>();

    /**
     * Creates a condition that will search all locations within the array.
     */
    public AnnotationListCondition() {
        this(ANY);
    }

    /**
     * Creates a condition that will search the given position within the array.
     */
    public AnnotationListCondition(int index) {
        this.index = index;
    }

    /**
     * Adds a condition that will be matched on the value in the array.
     */
    public AnnotationListCondition addCondition(AnnotationCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    @Override
    public String toString() {
        return "AnnotationListCondition{" +
                "index=" + index +
                ", conditions=" + conditions +
                '}';
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context, EvaluationStrategy strategy, JavaAnnotationTypeValueModel value) {
        if (!(value instanceof JavaAnnotationListTypeValueModel))
            return false;

        JavaAnnotationListTypeValueModel listTypeValueModel = (JavaAnnotationListTypeValueModel) value;

        List<JavaAnnotationTypeValueModel> selectedValues = getSelectedValues(listTypeValueModel);
        if (selectedValues == null)
            return false;

        boolean matched = false;
        for (AnnotationCondition condition : conditions) {
            for (JavaAnnotationTypeValueModel subValue : listTypeValueModel) {
                if (condition.evaluate(event, context, strategy, subValue)) {
                    matched = true;
                    break;
                }
            }

            if (matched)
                break;
        }

        return matched;
    }

    private List<JavaAnnotationTypeValueModel> getSelectedValues(JavaAnnotationListTypeValueModel listTypeValueModel) {
        List<JavaAnnotationTypeValueModel> selectedValues = new ArrayList<>();
        if (index == -1) {
            for (JavaAnnotationTypeValueModel listItem : listTypeValueModel) {
                selectedValues.add(listItem);
            }
        } else {
            try {
                selectedValues.add(Iterables.get(listTypeValueModel, index));
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        return selectedValues;
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (conditions != null) {
            for (AnnotationCondition condition : conditions) {
                result.addAll(condition.getRequiredParameterNames());
            }
        }

        return result;
    }
}
