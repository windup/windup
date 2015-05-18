package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents an &lt;env-ref&gt; entry from a Java deployment descriptor (eg, web.xml).
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(EnvironmentReferenceModel.TYPE)
public interface EnvironmentReferenceModel extends WindupVertexFrame
{
    public static final String REFERENCE_TYPE = "referenceType";
    public static final String NAME = "name";
    public static final String REFERENCE_ID = "referenceId";
    public static final String TYPE = "EnvironmentReferenceModel";

    /**
     * Contains the reference id
     */
    @Property(REFERENCE_ID)
    public String getReferenceId();

    /**
     * Contains the reference id
     */
    @Property(REFERENCE_ID)
    public void setReferenceId(String resourceId);

    /**
     * The reference's name
     */
    @Indexed
    @Property(NAME)
    public String getName();

    /**
     * The reference's name
     */
    @Property(NAME)
    public void setName(String name);

    /**
     * The reference type
     */
    @Property(REFERENCE_TYPE)
    public String getReferenceType();

    /**
     * The reference type
     */
    @Property(REFERENCE_TYPE)
    public void setReferenceType(String referenceType);

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    public JNDIResourceModel getJNDIReference();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    public void setJNDIReference(JNDIResourceModel jndiReference);

}
