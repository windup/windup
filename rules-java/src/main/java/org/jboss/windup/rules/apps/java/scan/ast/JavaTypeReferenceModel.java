package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.rules.files.model.FileLocationModel;

import com.tinkerpop.blueprints.Vertex;
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

    public static final String TYPE = "JavaTypeReference";
    public static final String REFERENCE_TYPE = "referenceType";

    /**
     * Contains the {@link TypeReferenceLocation} location referred to by this {@link Vertex}.
     */
    @Property(REFERENCE_TYPE)
    TypeReferenceLocation getReferenceLocation();

    /**
     * Contains the {@link TypeReferenceLocation} location referred to by this {@link Vertex}.
     */
    @Property(REFERENCE_TYPE)
    void setReferenceLocation(TypeReferenceLocation type);

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

            return location.toReadablePrefix() + " '" + getSourceSnippit() + "'";
        }
    }
}
