package org.jboss.windup.graph.model;

import com.tinkerpop.frames.modules.javahandler.JavaHandler;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface HasApplications extends BelongsToProject
{
    /**
     * Gets all root project models for current model (This will be mostly 1, but there are few exceptions which have multiple project models, so it
     * returns Iterable to keep interface consistent)
     *
     * @return root project models
     */
    Iterable<ProjectModel> getApplications();

    @Override
    @JavaHandler
    boolean belongsToProject(ProjectModel projectModel);

    abstract class Impl implements HasApplications, JavaHandlerContext<Vertex>, BelongsToProject
    {
        @Override
        public boolean belongsToProject(ProjectModel project)
        {
            ProjectModel canonicalProjectModel = this.getCanonicalProjectModel(project);

            for (ProjectModel currentProject : this.getApplications())
            {
                if (currentProject.equals(canonicalProjectModel))
                {
                    return true;
                }
            }

            return false;
        }
    }
}
