package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.FileLocationModel;

import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * The result of the XmlFile condition
 */
@TypeValue(XmlTypeReferenceModel.TYPE)
public interface XmlTypeReferenceModel extends FileLocationModel
{
    public static final String XPATH = "xpath";
    public static final String NAMESPACES = "namespaces";
    public static final String TYPE = "XmlTypeReferenceModel";

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
