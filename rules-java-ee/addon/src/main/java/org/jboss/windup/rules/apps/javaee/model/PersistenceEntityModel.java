package org.jboss.windup.rules.apps.javaee.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.HasApplications;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

/**
 * A common super model for EjbEntityBeanModel, JPAEntityModel and HibernateEntityModel.
 * TODO: this could go further - PersistenceOrmEntityModel to get more common things from HibernateEntityModel.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(PersistenceEntityModel.TYPE)
public interface PersistenceEntityModel extends WindupVertexFrame, HasApplications {
    String TYPE = "PersistenceEntity";

    String APPLICATIONS = TYPE + "-applications";
    String ENTITY_NAME = TYPE + "-entityName";
    String JPA_ENTITY_CLASS = TYPE + "-jpaEntityClass";
    String TABLE_NAME = TYPE + "-tableName";

    /**
     * Contains the application in which this JPA entity was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    Iterable<ProjectModel> getApplications();

    /**
     * Contains the entity name
     */
    @Property(value = ENTITY_NAME)
    String getEntityName();

    /**
     * Contains the entity class
     */
    @Adjacency(label = JPA_ENTITY_CLASS, direction = Direction.OUT)
    JavaClassModel getJavaClass();

    /**
     * Contains the application in which this JPA entity was discovered.
     */
    @Adjacency(label = APPLICATIONS, direction = Direction.OUT)
    void setApplications(Iterable<ProjectModel> applications);

    /**
     * Contains the entity name
     */
    @Property(value = ENTITY_NAME)
    void setEntityName(String entityName);

    /**
     * Contains the entity class
     */
    @Adjacency(label = JPA_ENTITY_CLASS, direction = Direction.OUT)
    void setJavaClass(JavaClassModel ejbHome);

    /**
     * Contains the name of the Table used by this Entity
     */
    @Property(TABLE_NAME)
    String getTableName();

    /**
     * Contains the name of the Table used by this Entity
     */
    @Property(TABLE_NAME)
    void setTableName(String tableName);

}
