package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.files.model.FileLocationModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This references a particular location within a Java source file, as well as the contents of that location.
 */
@TypeValue(JavaTypeReferenceModel.TYPE)
public interface JavaTypeReferenceModel extends FileLocationModel
{

    String TYPE = "JavaTypeReference";
    String REFERENCE_TYPE = "referenceType";
    String RESOLVED_SOURCE_SNIPPIT = "resolvedSourceSnippit";
    String RESOLUTION_STATUS = "resolutionStatus";

    /**
     * Overrides the default behavior to get the {@link JavaSourceFileModel} directly.
     */
    @Override
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    AbstractJavaSourceModel getFile();

    /**
     * Gets the snippit referenced by this {@link FileLocationModel}.
     */
    @Property(RESOLVED_SOURCE_SNIPPIT)
    @Indexed(IndexType.SEARCH)
    void setResolvedSourceSnippit(String source);

    /**
     * Sets the snippit referenced by this {@link FileLocationModel}.
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

    /**
     * Gets a human readable description of the location in the file
     */
    @JavaHandler
    String getDescription();

    abstract class Impl implements JavaTypeReferenceModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getDescription()
        {
            TypeReferenceLocation location = getReferenceLocation();

            return location.toReadablePrefix() + " '" + getResolvedSourceSnippit() + "'";
        }
    }
}
