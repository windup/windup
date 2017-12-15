package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(HibernateConfigurationFileModel.TYPE)
public interface HibernateConfigurationFileModel extends XmlFileModel
{

    String HIBERNATE_SESSION_FACTORY = "hibernateSessionFactory";
    String SPECIFICATION_VERSION = "specificationVersion";
    String TYPE = "HibernateConfigurationFileModel";

    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    @Adjacency(label = HIBERNATE_SESSION_FACTORY, direction = Direction.OUT)
    Iterable<HibernateSessionFactoryModel> getHibernateSessionFactories();

    @Adjacency(label = HIBERNATE_SESSION_FACTORY, direction = Direction.OUT)
    void addHibernateSessionFactory(HibernateSessionFactoryModel hibernateSessionFactor);
}
