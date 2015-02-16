package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(HibernateConfigurationFileModel.TYPE)
public interface HibernateConfigurationFileModel extends XmlFileModel
{

    public static final String HIBERNATE_SESSION_FACTORY = "hibernateSessionFactory";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String TYPE = "HibernateConfigurationFileModel";

    @Property(SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    @Property(SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    @Adjacency(label = HIBERNATE_SESSION_FACTORY, direction = Direction.OUT)
    public Iterable<HibernateSessionFactoryModel> getHibernateSessionFactories();

    @Adjacency(label = HIBERNATE_SESSION_FACTORY, direction = Direction.OUT)
    public void addHibernateSessionFactory(HibernateSessionFactoryModel hibernateSessionFactor);
}
