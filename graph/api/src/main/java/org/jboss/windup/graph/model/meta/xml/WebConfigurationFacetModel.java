package org.jboss.windup.graph.model.meta.xml;

import org.jboss.windup.reporting.renderer.api.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("WebFacet")
public interface WebConfigurationFacetModel extends XmlMetaFacetModel
{

    @Label
    @Property("specificationVersion")
    public String getSpecificationVersion();

    @Property("specificationVersion")
    public void setSpecificationVersion(String version);

    @Property("displayName")
    public String getDisplayName();

    @Property("displayName")
    public void setDisplayName(String displayName);

}
