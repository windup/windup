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
 */
@TypeValue(EnvironmentReferenceModel.TYPE)
public interface EnvironmentReferenceModel extends WindupVertexFrame
{
    public static final String TYPE = "EnvironmentReferenceModel";

    public static final String REFERENCE_TYPE = "referenceType";
    public static final String NAME = "name"; // Keeping this without prefix to spare an extra index.
    public static final String REFERENCE_ID = "referenceId";
    public static final String TAG_TYPE = "referenceTagType";

    /**
     * Contains the reference id
     */
    @Indexed
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
     * The reference type
     */
    @Property(TAG_TYPE)
    public EnvironmentReferenceTagType getReferenceTagType();

    /**
     * The reference type
     */
    @Property(TAG_TYPE)
    public void setReferenceTagType(EnvironmentReferenceTagType referenceType);


    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    public JNDIResourceModel getJndiReference();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    public void setJndiReference(JNDIResourceModel jndiReference);

}
