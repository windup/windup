package org.jboss.windup.rules.apps.xml.model;

import java.util.Map;

import org.jboss.windup.reporting.model.FileLocationModel;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XmlTypeReference")
public interface XmlTypeReferenceModel extends FileLocationModel
{
        public static final String PROPERTY_XPATH = "xpath";
        public static final String PROPERTY_NAMESPACES = "namespaces";

        @Property(PROPERTY_XPATH)
        public String getXpath();

        @Property(PROPERTY_XPATH)
        public void setXpath(String xpath);
        
        @Adjacency(label = PROPERTY_NAMESPACES)
        Iterable<NamespaceMetaModel> getNamespaces();
        
        @Adjacency(label = PROPERTY_NAMESPACES)
        void setNamespaces(Iterable<NamespaceMetaModel> children);

        @Adjacency(label = PROPERTY_NAMESPACES)
        NamespaceMetaModel addNamespace(NamespaceMetaModel friend);
        
}
