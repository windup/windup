package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.BelongsToProject;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Hibernate Session Factories.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
@TypeValue(HibernateSessionFactoryModel.TYPE)
public interface HibernateSessionFactoryModel extends WindupVertexFrame, BelongsToProject
{
    String TYPE = "HibernateSessionFactory";

    String DATASOURCE = "datasource";

    /**
     * Contains a link back to the {@link HibernateConfigurationFileModel} containing these properties
     */
    @Adjacency(label = HibernateConfigurationFileModel.HIBERNATE_SESSION_FACTORY, direction = Direction.IN)
    HibernateConfigurationFileModel getHibernateConfigurationFileModel();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DATASOURCE, direction = Direction.OUT)
    public Iterable<DataSourceModel> getDataSources();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DATASOURCE, direction = Direction.OUT)
    void addDataSource(DataSourceModel datasource);

    /**
     * Contains the hibernate session factories properties
     */
    @MapInAdjacentProperties(label = "sessionFactoryProperties")
    Map<String, String> getSessionFactoryProperties();

    /**
     * Contains the hibernate session factories properties
     */
    @MapInAdjacentProperties(label = "sessionFactoryProperties")
    void setSessionFactoryProperties(Map<String, String> map);

    @Override
    @JavaHandler
    boolean belongsToProject(ProjectModel projectModel);

    @Override
    @JavaHandler
    Iterable<ProjectModel> getRootProjectModels();

    abstract class Impl implements HibernateSessionFactoryModel, BelongsToProject, JavaHandlerContext<Vertex>
    {
        @Override
        public boolean belongsToProject(ProjectModel projectModel)
        {
            return this.getHibernateConfigurationFileModel().belongsToProject(projectModel);
        }

        @Override
        public Iterable<ProjectModel> getRootProjectModels()
        {
            return this.getHibernateConfigurationFileModel().getRootProjectModels();
        }
    }
}
