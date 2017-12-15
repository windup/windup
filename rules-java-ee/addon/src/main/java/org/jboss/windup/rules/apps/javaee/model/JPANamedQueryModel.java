package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
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
public interface JPANamedQueryModel extends WindupVertexFrame, HasApplications
{
    String QUERY_NAME = "queryName";
    String QUERY = "query";
    String TYPE = "JPANamedQueryModel";


    /**
     * Contains the query name
     */
    @Property(QUERY_NAME)
    String getQueryName();

    /**
     * Contains the query name
     */
    @Property(QUERY_NAME)
    void setQueryName(String queryName);

    
    /**
     * Contains the query
     */
    @Property(QUERY)
    String getQuery();

    /**
     * Contains the query
     */
    @Property(QUERY)
    void setQuery(String query);

    /**
     * Contains the jpa entity model
     */
    @Adjacency(label = JPAEntityModel.NAMED_QUERY, direction = Direction.IN)
    void setJpaEntity(JPAEntityModel jpaEntity);

    /**
     * Contains the jpa entity model
     */
    @Adjacency(label = JPAEntityModel.NAMED_QUERY, direction = Direction.IN)
    JPAEntityModel getJpaEntity();

    @JavaHandler
    @Override
    Iterable<ProjectModel> getApplications();

    @JavaHandler
    @Override
    boolean belongsToProject(ProjectModel projectModel);


    abstract class Impl implements JPANamedQueryModel, JavaHandlerContext<Vertex>
    {
        public Iterable<ProjectModel> getApplications()
        {
            return this.getJpaEntity().getApplications();
        }

        public boolean belongsToProject(ProjectModel projectModel)
        {
            return this.getJpaEntity().belongsToProject(projectModel);
        }
    }
}
