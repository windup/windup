package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

/**
 * Represents a JDNI resource found within the application.
 */
@TypeValue(JNDIResourceModel.TYPE)
public interface JNDIResourceModel extends WindupVertexFrame, HasApplications {
    String TYPE = "JNDIResourceModel";
    String JNDI_LOCATION = "JNDI_LOCATION";
    String APPLICATIONS = TYPE + "-application";

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    List<ProjectModel> getApplications();

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void setApplications(Iterable<ProjectModel> applications);

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void addApplication(ProjectModel application);

    /**
     * Indicates whether this {@link JNDIResourceModel} is associated with the given application.
     */
    default boolean isAssociatedWithApplication(ProjectModel application) {
        boolean alreadyExists = false;
        for (ProjectModel existing : getApplications()) {
            if (existing.equals(application)) {
                alreadyExists = true;
                break;
            }
        }
        return alreadyExists;
    }

    /**
     * Contains JNDI Location
     */
    @Indexed
    @Property(JNDI_LOCATION)
    String getJndiLocation();

    /**
     * Contains JNDI Location
     */
    @Property(JNDI_LOCATION)
    void setJndiLocation(String jndiLocation);

}
