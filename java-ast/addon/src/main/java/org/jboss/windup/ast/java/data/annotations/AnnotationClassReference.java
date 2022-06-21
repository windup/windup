package org.jboss.windup.ast.java.data.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;

/**
 * Contains annotation information, including a hierarchy of all of the properties defined by this annotation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class AnnotationClassReference extends ClassReference implements AnnotationValue {
    private ClassReference originalReference;
    private Map<String, AnnotationValue> annotationValues = new HashMap<>();

    /**
     * Indicates that an annotation with the give qualified name is specified at the given line, column, and of the provided length.
     */
    public AnnotationClassReference(ClassReference originalReference, String qualifiedName, String packageName, String className, ResolutionStatus resolutionStatus, int lineNumber,
                                    int column, int length, String line) {
        super(qualifiedName, packageName, className, null, resolutionStatus, TypeReferenceLocation.ANNOTATION, lineNumber, column, length, line);
        this.originalReference = originalReference;
    }

    /**
     * Gets the item referred to by this Annotation (for example, a method, type, or member variable declaration).
     */
    public ClassReference getOriginalReference() {
        return originalReference;
    }

    /**
     * If the item found is the use of an Annotation, then this will contain a map with the values used by the annotation.
     * <p>
     * Nested values are not currently supported here.
     */
    public void setAnnotationValues(Map<String, AnnotationValue> annotationValues) {
        this.annotationValues = annotationValues;
    }

    /**
     * If the item found is the use of an Annotation, then this will contain a map with the values used by the annotation.
     * <p>
     * Nested values are not currently supported here.
     */
    public Map<String, AnnotationValue> getAnnotationValues() {
        return Collections.unmodifiableMap(annotationValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((annotationValues == null) ? 0 : annotationValues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnnotationClassReference other = (AnnotationClassReference) obj;
        if (annotationValues == null) {
            if (other.annotationValues != null)
                return false;
        } else if (!annotationValues.equals(other.annotationValues))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AnnotationClassReference [" + super.toString() + ", annotationValues=" + annotationValues + "]";
    }
}
