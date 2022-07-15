package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.FileLocationModel;

import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;

/**
 * The result of the XmlFile condition
 */
@TypeValue(XmlTypeReferenceModel.TYPE)
public interface XmlTypeReferenceModel extends FileLocationModel {
    String XPATH = "xpath";
    String NAMESPACES = "namespaces";
    String TYPE = "XmlTypeReferenceModel";

    @Property(XPATH)
    String getXpath();

    @Property(XPATH)
    void setXpath(String xpath);

    @Adjacency(label = NAMESPACES)
    List<NamespaceMetaModel> getNamespaces();

    @Adjacency(label = NAMESPACES)
    void setNamespaces(Iterable<NamespaceMetaModel> children);

    @Adjacency(label = NAMESPACES)
    NamespaceMetaModel addNamespace(NamespaceMetaModel friend);

}
