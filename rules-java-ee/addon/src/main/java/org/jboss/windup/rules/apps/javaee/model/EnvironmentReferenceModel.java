package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Represents an &lt;env-ref&gt; entry from a Java deployment descriptor (eg, web.xml).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(EnvironmentReferenceModel.TYPE)
public interface EnvironmentReferenceModel extends WindupVertexFrame {
    String TYPE = "EnvironmentReferenceModel";

    String REFERENCE_TYPE = "referenceType";
    String NAME = "name"; // Keeping this without prefix to spare an extra index.
    String REFERENCE_ID = "referenceId";
    String TAG_TYPE = "referenceTagType";

    /**
     * Contains the reference id
     */
    @Indexed
    @Property(REFERENCE_ID)
    String getReferenceId();

    /**
     * Contains the reference id
     */
    @Property(REFERENCE_ID)
    void setReferenceId(String resourceId);

    /**
     * The reference's name
     */
    @Indexed
    @Property(NAME)
    String getName();

    /**
     * The reference's name
     */
    @Property(NAME)
    void setName(String name);

    /**
     * The reference type
     */
    @Property(REFERENCE_TYPE)
    String getReferenceType();

    /**
     * The reference type
     */
    @Property(REFERENCE_TYPE)
    void setReferenceType(String referenceType);


    /**
     * The reference type
     */
    @Property(TAG_TYPE)
    EnvironmentReferenceTagType getReferenceTagType();

    /**
     * The reference type
     */
    @Property(TAG_TYPE)
    void setReferenceTagType(EnvironmentReferenceTagType referenceType);


    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    JNDIResourceModel getJndiReference();

    /**
     * Contains the jndi location for this resource.
     */
    @Adjacency(label = JNDIResourceModel.TYPE, direction = Direction.OUT)
    void setJndiReference(JNDIResourceModel jndiReference);
}
