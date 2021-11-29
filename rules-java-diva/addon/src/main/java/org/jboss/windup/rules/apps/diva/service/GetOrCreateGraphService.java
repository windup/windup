package org.jboss.windup.rules.apps.diva.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;

public class GetOrCreateGraphService<T extends WindupVertexFrame> extends GraphService<T> {

    public String TYPE;

    public GetOrCreateGraphService(GraphContext context, Class<T> type) {
        super(context, type);
        try {
            TYPE = (String) type.getField("TYPE").get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public T getOrCreate(String key, String value, String... kvs) {
        GraphTraversal<Vertex, Vertex> pipeline;
        pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V();
        pipeline.has(WindupFrame.TYPE_PROP, TYPE);
        pipeline.has(key, value);
        for (int k = 0; k < kvs.length;) {
            pipeline.has(kvs[k++], kvs[k++]);
        }

        if (pipeline.hasNext()) {
            T result = frame(pipeline.next());
            return result;
        } else {
            T model = create();
            model.setProperty(key, value);
            for (int k = 0; k < kvs.length;) {
                model.setProperty(kvs[k++], kvs[k++]);
            }
            return model;
        }
    }
}
