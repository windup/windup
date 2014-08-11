package org.jboss.windup.rules.apps.java.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.NamespaceMetaModel;

import com.thinkaurelius.titan.core.attribute.Text;

public class NamespaceService extends GraphService<NamespaceMetaModel>
{

    public NamespaceService(GraphContext context)
    {
        super(context, NamespaceMetaModel.class);
    }

    public NamespaceMetaModel createNamespaceSchemaLocation(String namespaceURI, String schemaLocation)
    {
        Iterable<NamespaceMetaModel> results = getGraphContext().getFramed().query()
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, NamespaceMetaModel.TYPE)
                    .has("namespaceURI", namespaceURI).has("schemaLocation", schemaLocation)
                    .vertices(NamespaceMetaModel.class);

        for (NamespaceMetaModel result : results)
        {
            return result;
        }

        // otherwise, create it.
        NamespaceMetaModel meta = getGraphContext().getFramed().addVertex(null, NamespaceMetaModel.class);
        meta.setSchemaLocation(schemaLocation);
        meta.setURI(namespaceURI);

        return meta;
    }

}
