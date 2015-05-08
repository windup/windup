package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(JPAConfigurationFileModel.TYPE)
public interface JPAConfigurationFileModel extends XmlFileModel
{
    public static final String JPA_PERSISTENCE_UNIT = "jpaPersistenceUnit";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String TYPE = "JPAConfigurationFileModel";

    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    @Adjacency(label = JPA_PERSISTENCE_UNIT, direction = Direction.OUT)
    public Iterable<JPAPersistenceUnitModel> getPersistenceUnits();

    @Adjacency(label = JPA_PERSISTENCE_UNIT, direction = Direction.OUT)
    public void addPersistenceUnit(JPAPersistenceUnitModel jpaPersistenceUnit);
}
