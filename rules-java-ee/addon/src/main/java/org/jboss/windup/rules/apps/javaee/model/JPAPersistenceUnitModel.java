package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;
import java.util.Map;

/**
 * Contains metadata related to JPA Persistence Units.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JPAPersistenceUnitModel.TYPE)
public interface JPAPersistenceUnitModel extends WindupVertexFrame, HasApplications {
    String TYPE = "JPAPersistenceUnitModel";

    String DATASOURCE = "datasource";
    String NAME = TYPE + "-name";
    String APPLICATION = "application";

    /**
     * Contains the application in which this JPA persistence unit was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    ProjectModel getApplication();

    /**
     * Contains the application in which this JPA persistence unit was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    void setApplication(ProjectModel projectModel);

    /**
     * Contains persistence unit name
     */
    @Property(NAME)
    String getName();

    /**
     * Contains persistence unit name
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DATASOURCE, direction = Direction.OUT)
    List<DataSourceModel> getDataSources();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DATASOURCE, direction = Direction.OUT)
    void addDataSource(DataSourceModel dataSource);

    /**
     * Contains a link back to the {@link JPAConfigurationFileModel} containing these properties
     */
    @Adjacency(label = JPAConfigurationFileModel.JPA_PERSISTENCE_UNIT, direction = Direction.IN)
    JPAConfigurationFileModel getJPAConfigurationFileModel();

    /**
     * Contains the jpa persistence unit properties
     */
    @MapInAdjacentProperties(label = "persistenceUnitProperties")
    Map<String, String> getProperties();

    /**
     * Contains the jpa persistence unit properties
     */
    @MapInAdjacentProperties(label = "persistenceUnitProperties")
    void setProperties(Map<String, String> map);

    @JavaHandler(handler = Impl.class)
    @Override
    List<ProjectModel> getApplications();

    @JavaHandler(handler = Impl.class)
    @Override
    boolean belongsToProject(ProjectModel projectModel);

    class Impl {
        public List<ProjectModel> getApplications(JPAPersistenceUnitModel model) {
            return model.getJPAConfigurationFileModel().getApplications();
        }

        public boolean belongsToProject(JPAPersistenceUnitModel model, ProjectModel projectModel) {
            return model.getJPAConfigurationFileModel().belongsToProject(projectModel);
        }
    }
}
