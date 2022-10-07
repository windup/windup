package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

import java.util.List;

/**
 * Marker / base interface for Remote Services.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RemoteServiceModel.TYPE)
public interface RemoteServiceModel extends WindupVertexFrame, HasApplications {
    String TYPE = "RemoteServiceModel";

    String APPLICATIONS = "application";

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    List<ProjectModel> getApplications();

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void addApplication(ProjectModel application);

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void setApplications(Iterable<ProjectModel> applications);

    /**
     * Indicates whether this {@link JNDIResourceModel} is associated with the given application.
     */
    default boolean isAssociatedWithApplication(ProjectModel application) {
        for (ProjectModel existing : getApplications()) {
            if (existing.equals(application))
                return true;
        }
        return false;
    }
}
