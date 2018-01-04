package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Contains methods for loading, querying, and deleting {@link LinkModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class LinkService extends GraphService<LinkModel>
{
    /**
     * Constructs a {@link LinkService} instance.
     */
    public LinkService(GraphContext context)
    {
        super(context, LinkModel.class);
    }

    /**
     * Tries to find a link with the specified description and href. If it cannot, then it will return a new one.
     */
    public LinkModel getOrCreate(String description, String href)
    {
        Iterable<Vertex> results = getTypedQuery().has(LinkModel.PROPERTY_DESCRIPTION, description).has(LinkModel.PROPERTY_LINK, href).vertices();
        if (!results.iterator().hasNext())
        {
            LinkModel model = create();
            model.setDescription(description);
            model.setLink(href);
            return model;
        }
        return frame(results.iterator().next());
    }

}
