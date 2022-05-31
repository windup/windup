package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;
import java.util.Map;

/**
 * Contains metadata related to Hibernate Session Factories.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(HibernateSessionFactoryModel.TYPE)
public interface HibernateSessionFactoryModel extends WindupVertexFrame, HasApplications {
    String TYPE = "HibernateSessionFactoryModel";

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
    List<DataSourceModel> getDataSources();

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
    @JavaHandler(handler = Impl.class)
    boolean belongsToProject(ProjectModel projectModel);

    class Impl {
        public boolean belongsToProject(HibernateSessionFactoryModel model, ProjectModel projectModel) {
            return model.getHibernateConfigurationFileModel().belongsToProject(projectModel);
        }
    }
}
