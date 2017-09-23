package org.jboss.windup.reporting.service;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;

import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportPunchCardModel;

/**
 * Contains methods for finding, creating, and deleting {@link TagModel} instances.
 * TODO: Rename the other TagService to TagLoaderService?
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class TagGraphService extends GraphService<TagModel>
{
    public static final Logger LOG = Logger.getLogger(TagGraphService.class.getName());

    public TagGraphService(GraphContext context)
    {
        super(context, TagModel.class);
    }

    public TagModel getTagByName(String name) {
        if (null == name)
            throw new IllegalArgumentException("Looking for a null tag name.");
        return getUniqueByProperty(TagModel.PROP_NAME, name.toLowerCase());
    }

    /**
     * Traverses the graph structure and stores all tags to the graph.
     * <p>
     * There may be several isolated tag structures so we need to iterate through the root tags.
     */
    public void feedTheWholeTagStructureToGraph(org.jboss.windup.config.tags.TagService tagLoaderService)
    {

        Set<Tag> visited = new HashSet<>();

        for (Tag tag : tagLoaderService.getPrimeTags())
        {
            // Sanity check
            TagModel existing = this.getUniqueByProperty(TagModel.PROP_NAME, tag.getName());
            if (null != existing)
            {
                LOG.warning("TagModel already exists in graph, skipping root Tag: " + tag.getName());
                return;
            }

            int level = 0;
            feedTagStructureToGraph(tag, visited, level);
        }
    }

    /**
     * Creates and returns the TagModel in a graph as per given Tag;
     * recursively creates the designated ("contained") tags.
     * <p>
     * If the Tag was already processed exists, returns the corresponding TagModel.
     * Doesn't check if the TagModel for given tag name already exists, assuming this method is only called once.
     */
    private TagModel feedTagStructureToGraph(Tag tag, Set<Tag> visited, int level)
    {
        if (visited.contains(tag))
            return this.getUniqueByProperty(TagModel.PROP_NAME, tag.getName());
        visited.add(tag);

        LOG.info("Creating TagModel for Tag: " + StringUtils.repeat(' ', level*2) + tag.getName() + "("+tag.getContainedTags().size()+") " + tag.getTitle());
        TagModel tagModel = this.create().setName(tag.getName()).setTitle(tag.getTitle()).setColor(tag.getColor()).setRoot(tag.isPrime()).setPseudo(tag.isPseudo());

        tag.getContainedTags().forEach(tag2 -> {
            TagModel tag2model = feedTagStructureToGraph(tag2, visited, level+1);
            tagModel.addDesignatedTag(tag2model);
        });

        return tagModel;
    }
}
