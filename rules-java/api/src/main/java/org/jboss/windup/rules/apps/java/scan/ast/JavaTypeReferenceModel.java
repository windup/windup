package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;

/**
 * This references a particular location within a Java source file, as well as the contents of that location.
 */
@TypeValue(JavaTypeReferenceModel.TYPE)
public interface JavaTypeReferenceModel extends FileLocationModel
{

    String TYPE = "JavaTypeReferenceModel";
    String REFERENCE_TYPE = "referenceType";
    String RESOLVED_SOURCE_SNIPPIT = "resolvedSourceSnippit";
    String RESOLUTION_STATUS = "resolutionStatus";

    /**
     * Contains the annotations linked to this item.
     */
    @Adjacency(label = JavaAnnotationTypeReferenceModel.ORIGINAL_ANNOTATED_TYPE, direction = Direction.IN)
    List<JavaAnnotationTypeReferenceModel> getAnnotations();

    /**
     * Overrides the default behavior to get the {@link JavaSourceFileModel} directly.
     */
    @Override
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    AbstractJavaSourceModel getFile();

    /**
     * Sets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(RESOLVED_SOURCE_SNIPPIT)
    @Indexed(IndexType.SEARCH)
    void setResolvedSourceSnippit(String source);

    /**
     * Gets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(RESOLVED_SOURCE_SNIPPIT)
    String getResolvedSourceSnippit();
    
    /**
     * Contains the {@link TypeReferenceLocation} location referred to by this {@link Vertex}.
     */
    @Property(REFERENCE_TYPE)
    TypeReferenceLocation getReferenceLocation();

    /**
     * Contains the {@link TypeReferenceLocation} location referred to by this {@link Vertex}.
     */
    @Property(REFERENCE_TYPE)
    @Indexed(IndexType.SEARCH)
    void setReferenceLocation(TypeReferenceLocation type);

    /**
     * Indicates whether or not we were able to resolve this reference based upon information available on the classpath
     */
    @Property(RESOLUTION_STATUS)
    ResolutionStatus getResolutionStatus();

    /**
     * Indicates whether or not we were able to resolve this reference based upon information available on the classpath
     */
    @Property(RESOLUTION_STATUS)
    @Indexed
    void setResolutionStatus(ResolutionStatus status);    
    
    @Property("returnType")
    @Indexed
    String getReturnType();    
    
    @Property("returnType")
    @Indexed
    String setReturnType(String returnType);
    
    

    /**
     * Gets a human readable description of the location in the file
     */
    default String getDescription()
    {
        TypeReferenceLocation location = getReferenceLocation();

        return location.toReadablePrefix() + " '" + getResolvedSourceSnippit() + "'";
    }

}
