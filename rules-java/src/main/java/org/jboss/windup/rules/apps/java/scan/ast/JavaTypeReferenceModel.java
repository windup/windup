package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.reporting.model.FileLocationModel;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This references a particular location within a Java source file, as well as the contents of that location.
 */
@TypeValue(JavaTypeReferenceModel.TYPE)
public interface JavaTypeReferenceModel extends FileLocationModel
{

    public static final String TYPE = "JavaTypeReference";
    public static final String REFERENCE_TYPE = "referenceType";
    public static final String SOURCE_SNIPPIT = "referenceSourceSnippit";

    @Property(REFERENCE_TYPE)
    public TypeReferenceLocation getReferenceLocation();

    @Property(REFERENCE_TYPE)
    public void setReferenceLocation(TypeReferenceLocation type);

    @Property(SOURCE_SNIPPIT)
    public void setSourceSnippit(String source);

    @Property(SOURCE_SNIPPIT)
    public String getSourceSnippit();
}
