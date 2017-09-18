package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a JDNI resource found within the application.
 */
@TypeValue(JNDIResourceModel.TYPE)
public interface JNDIResourceModel extends WindupVertexFrame, HasApplications
{
    String TYPE = "JNDIResourceModel";
    String JNDI_LOCATION = "JNDI_LOCATION";
    String APPLICATIONS = TYPE + "-application";

    /**
     * Contains the application in which this JNDI resource was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    Iterable<ProjectModel> getApplications();

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
    @JavaHandler
    boolean isAssociatedWithApplication(ProjectModel application);

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

    abstract class Impl implements JNDIResourceModel, JavaHandlerContext<Vertex>
    {
        public boolean isAssociatedWithApplication(ProjectModel application)
        {
            boolean alreadyExists = false;
            for (ProjectModel existing : getApplications())
            {
                if (existing.equals(application))
                {
                    alreadyExists = true;
                    break;
                }
            }
            return alreadyExists;
        }
    }
}
