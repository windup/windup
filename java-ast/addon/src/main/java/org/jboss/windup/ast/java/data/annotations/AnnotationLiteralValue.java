package org.jboss.windup.ast.java.data.annotations;

/**
 * Contains a literal value from an annotation, as well as the type of the literal.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationLiteralValue implements AnnotationValue {

    private final Class<?> type;
    private final Object value;

    /**
     * Instantiates a {@link AnnotationLiteralValue} with the give type and value.
     */
    public AnnotationLiteralValue(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Contains the literal type.
     */
    public Class<?> getLiteralType() {
        return type;
    }

    /**
     * Contains the literal value.
     */
    public Object getLiteralValue() {
        return value;
    }
}
