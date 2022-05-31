package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.model.QuickfixType;

/**
 * Contains methods for loading, querying, and deleting {@link QuickfixModel}s.
 */
public class QuickfixService extends GraphService<QuickfixModel> {
    /**
     * Constructs a {@link QuickfixService} instance.
     */
    public QuickfixService(GraphContext context) {
        super(context, QuickfixModel.class);
    }

    /**
     * Tries to find a link with the specified description and href. If it cannot, then it will return a new one.
     */
    @SuppressWarnings("unchecked")
    public QuickfixModel getOrCreate(String name, QuickfixType type) {
        Iterable<Vertex> results = (Iterable<Vertex>) getQuery().getRawTraversal().has(QuickfixModel.PROPERTY_TYPE, type).has(QuickfixModel.PROPERTY_DESCRIPTION, name).toList();
        if (!results.iterator().hasNext()) {
            QuickfixModel model = create();
            model.setQuickfixType(type);
            model.setName(name);
            return model;
        }
        return frame(results.iterator().next());
    }

}
