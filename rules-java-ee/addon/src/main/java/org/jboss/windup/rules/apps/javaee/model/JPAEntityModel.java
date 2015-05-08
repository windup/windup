package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata associated with a JPA Entity
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
@TypeValue(JPAEntityModel.TYPE)
public interface JPAEntityModel extends WindupVertexFrame
{
    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";
    public static final String TABLE_NAME = "tableName";
    public static final String JPA_ENTITY_CLASS = "jpaEntityClass";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String TYPE = "JPAEntityModel";

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    /**
     * Contains the specification version
     */
    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    /**
     * Contains the table name
     */
    @Property(TABLE_NAME)
    public String getTableName();

    /**
     * Contains the table name
     */
    @Property(TABLE_NAME)
    public void setTableName(String tableName);

    /**
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    public String getSchemaName();

    /**
     * Contains the schema name
     */
    @Property(SCHEMA_NAME)
    public void setSchemaName(String schemaName);

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    public String getCatalogName();

    /**
     * Contains the catalog name
     */
    @Property(CATALOG_NAME)
    public void setCatalogName(String catalogName);

    /**
     * Contains the entity class
     */
    @Adjacency(label = JPA_ENTITY_CLASS, direction = Direction.OUT)
    public void setJavaClass(JavaClassModel ejbHome);

    /**
     * Contains the entity class
     */
    @Adjacency(label = JPA_ENTITY_CLASS, direction = Direction.OUT)
    public JavaClassModel getJavaClass();
}
