package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Marker / base interface for Remote Services.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(RemoteServiceModel.TYPE)
public interface RemoteServiceModel extends WindupVertexFrame, HasApplications
{
    String TYPE = "RemoteService";

    String APPLICATIONS = "application";

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

    abstract class Impl implements RemoteServiceModel, JavaHandlerContext<Vertex>
    {
        public boolean isAssociatedWithApplication(ProjectModel application)
        {
            for (ProjectModel existing : getApplications())
            {
                if (existing.equals(application))
                    return true;
            }
            return false;
        }
    }
}
