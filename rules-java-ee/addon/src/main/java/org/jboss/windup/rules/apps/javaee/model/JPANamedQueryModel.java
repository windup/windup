package org.jboss.windup.rules.apps.javaee.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

/**
 * Contains metadata associated with a JPA Named Query.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JPANamedQueryModel.TYPE)
public interface JPANamedQueryModel extends WindupVertexFrame, HasApplications {
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
    JPAEntityModel getJpaEntity();

    /**
     * Contains the jpa entity model
     */
    @Adjacency(label = JPAEntityModel.NAMED_QUERY, direction = Direction.IN)
    void setJpaEntity(JPAEntityModel jpaEntity);

    @JavaHandler(handler = Impl.class)
    @Override
    List<ProjectModel> getApplications();

    @JavaHandler(handler = Impl.class)
    @Override
    boolean belongsToProject(ProjectModel projectModel);


    class Impl {
        public List<ProjectModel> getApplications(JPANamedQueryModel model) {
            return model.getJpaEntity().getApplications();
        }

        public boolean belongsToProject(JPANamedQueryModel model, ProjectModel projectModel) {
            return model.getJpaEntity().belongsToProject(projectModel);
        }
    }
}
