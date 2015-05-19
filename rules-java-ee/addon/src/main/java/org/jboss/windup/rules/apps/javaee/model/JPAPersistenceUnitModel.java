package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to JPA Persistence Units.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
@TypeValue(JPAPersistenceUnitModel.TYPE)
public interface JPAPersistenceUnitModel extends WindupVertexFrame
{
    public static final String TYPE = "JPAPersistenceUnit";
    public static final String NAME = "persistenceUnitName";

    /**
     * Contains persistence unit name
     */
    @Property(NAME)
    public String getName();

    /**
     * Contains persistence unit name
     */
    @Property(NAME)
    public void setName(String name);

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DataSourceModel.DATA_SOURCE, direction = Direction.OUT)
    public Iterable<DataSourceModel> getDataSources();

    /**
     * Contains a link back to the {@link DataSourceModel}
     */
    @Adjacency(label = DataSourceModel.DATA_SOURCE, direction = Direction.OUT)
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
}
