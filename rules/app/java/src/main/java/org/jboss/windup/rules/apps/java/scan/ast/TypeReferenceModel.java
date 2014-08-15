package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.reporting.model.FileLocationModel;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaTypeReference")
public interface TypeReferenceModel extends FileLocationModel
{

    public static final String PROPERTY_REFERENCE_TYPE = "referenceType";
    public static final String PROPERTY_LINE_NUMBER = "referenceLineNumber";
    public static final String PROPERTY_START_COLUMN = "referenceStartColumn";
    public static final String PROPERTY_LENGTH = "referenceLength";
    public static final String PROPERTY_SOURCE_SNIPPIT = "referenceSourceSnippit";

    @Property(PROPERTY_REFERENCE_TYPE)
    public TypeReferenceLocation getReferenceLocation();

    @Property(PROPERTY_REFERENCE_TYPE)
    public void setReferenceLocation(TypeReferenceLocation type);

    @Property(PROPERTY_SOURCE_SNIPPIT)
    public void setSourceSnippit(String source);

    @Property(PROPERTY_SOURCE_SNIPPIT)
    public String getSourceSnippit();
}
