package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata associated with a JPA Named Query.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
@TypeValue(JPANamedQueryModel.TYPE)
public interface JPANamedQueryModel extends WindupVertexFrame
{
    public static final String JPA_ENTITY = "jpaEntity";
    public static final String QUERY_NAME = "queryName";
    public static final String QUERY = "query";
    public static final String TYPE = "JPANamedQuery";


    /**
     * Contains the query name
     */
    @Property(QUERY_NAME)
    public String getQueryName();

    /**
     * Contains the query name
     */
    @Property(QUERY_NAME)
    public void setQueryName(String queryName);

    
    /**
     * Contains the query
     */
    @Property(QUERY)
    public String getQuery();

    /**
     * Contains the query
     */
    @Property(QUERY)
    public void setQuery(String query);

    /**
     * Contains the jpa entity model
     */
    @Adjacency(label = JPAEntityModel.NAMED_QUERY, direction = Direction.IN)
    public void setJpaEntity(JPAEntityModel jpaEntity);

    /**
     * Contains the jpa entity model
     */
    @Adjacency(label = JPAEntityModel.NAMED_QUERY, direction = Direction.IN)
    public JPAEntityModel getJpaEntity();
}
