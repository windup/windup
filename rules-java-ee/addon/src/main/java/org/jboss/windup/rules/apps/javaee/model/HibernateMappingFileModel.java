package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains metadata extracted from a hibernate mapping file (*.hbm.xml)
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(HibernateMappingFileModel.TYPE)
public interface HibernateMappingFileModel extends XmlFileModel
{
    public static final String TYPE = "HibernateMappingFileModel";
    public static final String HIBERNATE_ENTITY = "hibernateEntity";
    public static final String SPECIFICATION_VERSION = "specificationVersion";

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
     * Contains the list of all {@link HibernateEntityModel}s referenced by this mapping file
     */
    @Adjacency(label = HIBERNATE_ENTITY, direction = Direction.OUT)
    public Iterable<HibernateEntityModel> getHibernateEntities();

    /**
     * Contains the list of all {@link HibernateEntityModel}s referenced by this mapping file
     */
    @Adjacency(label = HIBERNATE_ENTITY, direction = Direction.OUT)
    public void addHibernateEntity(HibernateEntityModel hibernateEntity);
}
