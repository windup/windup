package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface HasApplications extends BelongsToProject
{
    /**
     * Contains the application in which this JPA entity was discovered.
     */
    @Adjacency(label = PersistenceEntityModel.APPLICATIONS, direction = Direction.OUT)
    Iterable<ProjectModel> getApplications();


    @Override
    @JavaHandler
    boolean belongsToProject(ProjectModel projectModel);

    abstract class Impl implements HasApplications, JavaHandlerContext<Vertex>, BelongsToProject
    {
        @Override
        public boolean belongsToProject(ProjectModel project)
        {
            for (ProjectModel currentProject : this.getApplications())
            {
                if (currentProject.equals(project))
                {
                    return true;
                }
            }

            return false;
        }
    }
}
