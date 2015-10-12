package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(ThreadPoolModel.TYPE)
public interface ThreadPoolModel extends WindupVertexFrame
{
    public static final String POOL_NAME = "poolName";
    public static final String MIN_POOL_SIZE = "minPoolSize";
    public static final String MAX_POOL_SIZE = "maxPoolSize";
    public static final String TYPE = "ThreadPoolModel";

    /**
     * Max pool size
     */
    @Property(MAX_POOL_SIZE)
    public Integer getMaxPoolSize();

    /**
     * Max pool size
     */
    @Property(MAX_POOL_SIZE)
    public void setMaxPoolSize(Integer maxPoolSize);

    /**
     * Min pool size
     */
    @Property(MIN_POOL_SIZE)
    public Integer getMinPoolSize();

    /**
     * Min pool size
     */
    @Property(MIN_POOL_SIZE)
    public void setMinPoolSize(Integer minPoolSize);

    /**
     * Pool name
     */
    @Property(POOL_NAME)
    public String getPoolName();

    /**
     * Pool name
     */
    @Property(POOL_NAME)
    public void setPoolName(String poolName);

}
