package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface HasApplications extends BelongsToProject
{
    Iterable<ProjectModel> getApplications();

    @Override
    @JavaHandler
    boolean belongsToProject(ProjectModel projectModel);

    @Override
    @JavaHandler
    Iterable<ProjectModel> getRootProjectModels();

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

        @Override
        public Iterable<ProjectModel> getRootProjectModels()
        {
            Set<ProjectModel> projectModelSet = new HashSet<>();

            for (ProjectModel currentProjectModel : this.getApplications())
            {
                ProjectModel rootProjectModel = currentProjectModel.getRootProjectModel();
                projectModelSet.add(rootProjectModel);
            }

            return projectModelSet;
        }
    }
}
