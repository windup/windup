package org.jboss.windup.rules.apps.java.model.mixin;

import com.syncleus.ferma.annotations.Adjacency;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * A model mix-in for models that reference a Java type, for instance, resource-ref in web.xml.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
*/
@TypeValue(HasTypeModel.TYPE)
public interface HasTypeModel extends WindupVertexFrame
{
    String TYPE = "HasTypeModel";

    String JAVA_TYPE_NAME = TYPE + "-javaType";
    String JAVA_TYPE = TYPE + "-javaType";

    /**
     * The referenced type name.
     */
    @Property(JAVA_TYPE_NAME)
    String getJavaTypeName();

    /**
     * The referenced type name.
     */
    @Property(JAVA_TYPE_NAME)
    void setJavaTypeName(String clazz);

    /**
     * The referenced type, if available.
     */
    @Adjacency(label = JAVA_TYPE)
    JavaClassModel getJavaType();

    /**
     * The referenced type, if available.
     */
    @Adjacency(label = JAVA_TYPE)
    void setJavaType(JavaClassModel clazz);

}
