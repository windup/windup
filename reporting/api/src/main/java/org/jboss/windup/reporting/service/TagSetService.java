package org.jboss.windup.reporting.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TagSetModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Contains methods for getting tag set models as well as maintaining a cache.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TagSetService extends GraphService<TagSetModel>
{
    private static final String TAG_CACHE_KEY = TagSetService.class.getCanonicalName() + "_Cache";

    public TagSetService(GraphContext context)
    {
        super(context, TagSetModel.class);
    }

    private Map<Set<String>, Vertex> getCache(GraphRewrite event)
    {
        @SuppressWarnings("unchecked")
        Map<Set<String>, Vertex> result = (Map<Set<String>, Vertex>) event.getRewriteContext().get(TAG_CACHE_KEY);
        if (result == null)
        {
            result = new HashMap<>();
            event.getRewriteContext().put(TAG_CACHE_KEY, result);
        }
        return result;
    }

    /**
     * This essentially ensures that we only store a single Vertex for each unique "Set" of tags.
     */
    public TagSetModel getOrCreate(GraphRewrite event, Set<String> tags)
    {
        Map<Set<String>, Vertex> cache = getCache(event);
        Vertex vertex = cache.get(tags);
        if (vertex == null)
        {
            TagSetModel model = create();
            model.setTags(tags);
            cache.put(tags, model.asVertex());
            return model;
        }
        else
        {
            return frame(vertex);
        }
    }
}
