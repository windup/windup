package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(XmlFileModel.TYPE)
public interface XmlFileModel extends FileModel, SourceFileModel
{
    public static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    public static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed";

    public static final String ROOT_TAG_NAME = "rootTagName";
    public static final String NAMESPACE = "namespace";
    public static final String DOCTYPE = "doctype";
    public static final String TYPE = "XmlFileModel";

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    public void setDoctype(DoctypeMetaModel doctype);

    @Adjacency(label = DOCTYPE, direction = Direction.OUT)
    public DoctypeMetaModel getDoctype();

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    public void addNamespace(NamespaceMetaModel namespace);

    @Adjacency(label = NAMESPACE, direction = Direction.OUT)
    public Iterable<NamespaceMetaModel> getNamespaces();

    @Indexed
    @Property(ROOT_TAG_NAME)
    public String getRootTagName();

    @Property(ROOT_TAG_NAME)
    public void setRootTagName(String rootTagName);
}
