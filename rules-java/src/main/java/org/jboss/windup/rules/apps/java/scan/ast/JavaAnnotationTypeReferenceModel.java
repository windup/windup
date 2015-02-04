package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Java annotations (eg, attribute values).
 *
 */
@TypeValue(JavaAnnotationTypeReferenceModel.TYPE)
public interface JavaAnnotationTypeReferenceModel extends JavaTypeReferenceModel
{
    public static final String ANNOTATION_VALUE_MAP = "annotationValueMap";
    public static final String TYPE = "JavaAnnotationTypeReference";

    /**
     * Contains the values of attributes specified within the annotation.
     */
    @MapInAdjacentProperties(label = ANNOTATION_VALUE_MAP)
    void setAnnotationValues(Map<String, String> values);

    /**
     * Contains the values of attributes specified within the annotation.
     */
    @MapInAdjacentProperties(label = ANNOTATION_VALUE_MAP)
    Map<String, String> getAnnotationValues();
}
