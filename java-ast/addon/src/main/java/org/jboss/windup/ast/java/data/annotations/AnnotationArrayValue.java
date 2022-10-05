package org.jboss.windup.ast.java.data.annotations;

import java.util.List;

/**
 * Contains an array of {@link AnnotationValue}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationArrayValue implements AnnotationValue {

    private final List<AnnotationValue> values;

    /**
     * Creates an {@link AnnotationArrayValue} with the given list of values.
     */
    public AnnotationArrayValue(List<AnnotationValue> values) {
        this.values = values;
    }

    /**
     * Gets the values declared by this annotation.
     */
    public List<AnnotationValue> getValues() {
        return values;
    }
}
