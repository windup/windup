package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.rules.files.model.FileLocationModel;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * The result of the XmlFile condition
 */
@TypeValue("XmlTypeReference")
public interface XmlTypeReferenceModel extends FileLocationModel
{
    public static final String XPATH = "xpath";
    public static final String NAMESPACES = "namespaces";

    @Property(XPATH)
    String getXpath();

    @Property(XPATH)
    void setXpath(String xpath);

    @Adjacency(label = NAMESPACES)
    Iterable<NamespaceMetaModel> getNamespaces();

    @Adjacency(label = NAMESPACES)
    void setNamespaces(Iterable<NamespaceMetaModel> children);

    @Adjacency(label = NAMESPACES)
    NamespaceMetaModel addNamespace(NamespaceMetaModel friend);

}