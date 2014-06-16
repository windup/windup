package org.jboss.windup.graph.model;

import org.jboss.windup.rules.apps.ejb.model.HibernateEntityModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateConfigurationFacet")
public interface HibernateSessionFactoryModel extends VertexFrame
{

    @Property("name")
    public String getName();

    @Property("name")
    public void setName(String name);

    @Adjacency(label = "xmlResourceModel", direction = Direction.OUT)
    public void setXmlResourceModel(XmlResourceModel facet);

    @Adjacency(label = "xmlResourceModel", direction = Direction.OUT)
    public XmlResourceModel getXmlResourceModel();

    @Adjacency(label = "hibernateEntity", direction = Direction.OUT)
    public Iterable<HibernateEntityModel> getHibernateEntity();

    @Adjacency(label = "hibernateEntity", direction = Direction.OUT)
    public void addHibernateEntityReference(HibernateEntityModel hibernateEntity);

}
