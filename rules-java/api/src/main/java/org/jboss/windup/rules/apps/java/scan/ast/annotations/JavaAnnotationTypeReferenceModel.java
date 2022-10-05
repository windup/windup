package org.jboss.windup.rules.apps.java.scan.ast.annotations;

import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Contains metadata related to Java annotations (eg, attribute values).
 */
@TypeValue(JavaAnnotationTypeReferenceModel.TYPE)
public interface JavaAnnotationTypeReferenceModel extends JavaTypeReferenceModel, JavaAnnotationTypeValueModel {
    String ANNOTATION_VALUE_MAP = "annotationValueMap";
    String TYPE = "JavaAnnotationTypeReferenceModel";
    String ORIGINAL_ANNOTATED_TYPE = "originalAnnotatedType";

    /**
     * Contains the type that this annotation was placed on.
     */
    @Adjacency(label = ORIGINAL_ANNOTATED_TYPE, direction = Direction.OUT)
    JavaTypeReferenceModel getAnnotatedType();

    /**
     * Contains the type that this annotation was placed on.
     */
    @Adjacency(label = ORIGINAL_ANNOTATED_TYPE, direction = Direction.OUT)
    void setAnnotatedType(JavaTypeReferenceModel annotatedType);

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
