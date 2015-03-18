package org.jboss.windup.rules.apps.java.scan.ast.annotations;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Java annotations (eg, attribute values).
 *
 */
@TypeValue(JavaAnnotationTypeReferenceModel.TYPE)
public interface JavaAnnotationTypeReferenceModel extends JavaTypeReferenceModel, JavaAnnotationTypeValueModel
{
    public static final String ANNOTATION_VALUE_MAP = "annotationValueMap";
    public static final String TYPE = "JavaAnnotationTypeReference";

    /**
     * Contains the values of attributes specified within the annotation.
     */
    @MapInAdjacentVertices(label = ANNOTATION_VALUE_MAP)
    void setAnnotationValues(Map<String, JavaAnnotationTypeValueModel> values);

    /**
     * Contains the values of attributes specified within the annotation.
     */
    @MapInAdjacentVertices(label = ANNOTATION_VALUE_MAP)
    Map<String, JavaAnnotationTypeValueModel> getAnnotationValues();
}
