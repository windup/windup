package org.jboss.windup.rules.apps.web.model;

import org.jboss.windup.reporting.renderer.api.Label;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;


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
