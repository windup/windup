package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata associated with a Hibernate Entity
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
@TypeValue(HibernateEntityModel.TYPE)
public interface HibernateEntityModel extends WindupVertexFrame
{
    String CATALOG_NAME = "catalogName";
    String SCHEMA_NAME = "schemaName";
    String TABLE_NAME = "tableName";
    String SPECIFICATION_VERSION = "specificationVersion";
    String HIBERNATE_ENTITY_CLASS = "hibernateEntityClass";
    String TYPE = "HibernateEntityModel";
    String APPLICATION = "application";

    /**
     * Contains the application in which this Spring Bean was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    ProjectModel getApplication();

    /**
     * Contains the application in which this Spring Bean  was discovered.
     */
    @Adjacency(label = APPLICATION, direction = Direction.OUT)
    void setApplication(ProjectModel projectModel);

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * Contains the table name
     */
    @Property(TABLE_NAME)
    String getTableName();

    /**
     * Contains the table name
     */
    @Property(TABLE_NAME)
    void setTableName(String tableName);

    /**
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    String getSchemaName();

    /**
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    void setSchemaName(String schemaName);

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    String getCatalogName();

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    void setCatalogName(String catalogName);

    /**
     * Contains the entity class
     */
    @Adjacency(label = HIBERNATE_ENTITY_CLASS, direction = Direction.OUT)
    void setJavaClass(JavaClassModel entityClass);

    /**
     * Contains the entity class
     */
    @Adjacency(label = HIBERNATE_ENTITY_CLASS, direction = Direction.OUT)
    JavaClassModel getJavaClass();
}
