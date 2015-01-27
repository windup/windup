package org.jboss.windup.rules.apps.javaee.model;

import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata related to Hibernate Session Factories.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue(HibernateSessionFactoryModel.TYPE)
public interface HibernateSessionFactoryModel extends WindupVertexFrame
{

    public static final String TYPE = "HibernateSessionFactory";

    /**
     * Contains a link back to the {@link HibernateConfigurationFileModel} containing these properties
     */
    @Adjacency(label = HibernateConfigurationFileModel.HIBERNATE_SESSION_FACTORY, direction = Direction.IN)
    HibernateConfigurationFileModel getHibernateConfigurationFileModel();

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
}
