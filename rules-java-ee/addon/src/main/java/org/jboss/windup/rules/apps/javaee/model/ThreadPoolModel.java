package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains thread pool information (max pool size, pool name, etc).
 */
@TypeValue(ThreadPoolModel.TYPE)
public interface ThreadPoolModel extends WindupVertexFrame
{
    String TYPE = "ThreadPool";
    String POOL_NAME = "poolName";
    String MIN_POOL_SIZE = "minPoolSize";
    String MAX_POOL_SIZE = "maxPoolSize";
    String APPLICATION = "application";

    /**
     * Contains the application in which this thread pool was discovered
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    ProjectModel getApplication();

    /**
     * Contains the application in which this thread pool was discovered
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    void setApplication(ProjectModel projectModel);

    /**
     * Max pool size
     */
    @Property(MAX_POOL_SIZE)
    Integer getMaxPoolSize();

    /**
     * Max pool size
     */
    @Property(MAX_POOL_SIZE)
    void setMaxPoolSize(Integer maxPoolSize);

    /**
     * Min pool size
     */
    @Property(MIN_POOL_SIZE)
    Integer getMinPoolSize();

    /**
     * Min pool size
     */
    @Property(MIN_POOL_SIZE)
    void setMinPoolSize(Integer minPoolSize);

    /**
     * Pool name
     */
    @Property(POOL_NAME)
    String getPoolName();

    /**
     * Pool name
     */
    @Property(POOL_NAME)
    void setPoolName(String poolName);
}
