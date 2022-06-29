package org.jboss.windup.rules.apps.xml.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;

/**
 * Adds createNamespaceSchemaLocation().
 */
public class NamespaceService extends GraphService<NamespaceMetaModel> {
    public NamespaceService(GraphContext context) {
        super(context, NamespaceMetaModel.class);
    }

    public NamespaceMetaModel createNamespaceSchemaLocation(String namespaceURI, String schemaLocation) {
        Iterable<? extends NamespaceMetaModel> results = getGraphContext().getQuery(NamespaceMetaModel.class)
                .traverse(g -> g.has("namespaceURI", namespaceURI).has("schemaLocation", schemaLocation))
                .toList(NamespaceMetaModel.class);

        if (results.iterator().hasNext()) {
            return results.iterator().next();
        }

        // otherwise, create it.
        NamespaceMetaModel meta = getGraphContext().getFramed().addFramedVertex(NamespaceMetaModel.class);
        meta.setSchemaLocation(schemaLocation);
        meta.setURI(namespaceURI);

        return meta;
    }

}
