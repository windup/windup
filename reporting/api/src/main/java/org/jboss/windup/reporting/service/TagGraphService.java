package org.jboss.windup.reporting.service;

import java.util.*;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.util.exception.WindupException;


/**
 * Contains methods for finding, creating, and deleting {@link TagModel} instances.
 * TODO: Rename the other TagService to TagLoaderService?
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class TagGraphService extends GraphService<TagModel> {
    public static final Logger LOG = Logger.getLogger(TagGraphService.class.getName());

    public TagGraphService(GraphContext context) {
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
    public void feedTheWholeTagStructureToGraph(org.jboss.windup.config.tags.TagService tagLoaderService) {

        Set<Tag> visited = new HashSet<>();

        for (Tag tag : tagLoaderService.getRootTags()) {
            // Sanity check
            TagModel existing = this.getUniqueByProperty(TagModel.PROP_NAME, tag.getName());
            if (null != existing) {
                LOG.warning("TagModel already exists in graph, skipping root Tag: " + tag.getName());
                return;
            }

            int level = 0;
            this.feedTagStructureToGraph(tag, visited, level);
        }
    }

    /**
     * Creates and returns the TagModel in a graph as per given Tag;
     * recursively creates the designated ("contained") tags.
     * <p>
     * If the Tag was already processed exists, returns the corresponding TagModel.
     * Doesn't check if the TagModel for given tag name already exists, assuming this method is only called once.
     */
    private TagModel feedTagStructureToGraph(Tag tag, Set<Tag> visited, int level) {
        if (visited.contains(tag))
            return this.getUniqueByProperty(TagModel.PROP_NAME, tag.getName(), true);
        visited.add(tag);

        LOG.fine(String.format("Creating TagModel for Tag: %s%s(%d)   '%s'  traits: %s", StringUtils.repeat(' ', level * 2),
                tag.getName(), tag.getContainedTags().size(), tag.getTitle(), tag.getTraits()));
        TagModel tagModel = this.create();
        tagModel.setName(tag.getName());
        tagModel.setTitle(tag.getTitle());
        tagModel.setColor(tag.getColor());
        tagModel.setRoot(tag.isPrime());
        tagModel.setPseudo(tag.isPseudo());
        if (null != tag.getTraits())
            tagModel.putAllTraits(tag.getTraits());

        tag.getContainedTags().forEach(tag2 -> {
            TagModel tag2model = feedTagStructureToGraph(tag2, visited, level + 1);
            tagModel.addDesignatedTag(tag2model);
        });

        return tagModel;
    }

    /**
     * Returns all tags that are designated by this tag. E.g., for "vehicle", this would return "ship", "car", "tesla-model3", "bike", etc.
     */
    public Set<TagModel> getDescendantTags(TagModel tag) {
        Set<TagModel> ancestors = new HashSet<>();
        getDescendantTags(tag, ancestors);
        return ancestors;
    }

    private void getDescendantTags(TagModel tag, Set<TagModel> putResultsHere) {
        for (TagModel childTag : tag.getDesignatedTags()) {
            if (!putResultsHere.add(childTag))
                continue; // Already visited.
            getDescendantTags(childTag, putResultsHere);
        }
    }

    /**
     * @return true if the subTag is contained directly or indirectly in the superTag.
     */
    public static boolean isTagUnderTagOrSame(TagModel subTag, TagModel superTag) {
        return isTagUnderTag(subTag, superTag, true);
    }

    public static boolean isTagUnderTag(TagModel subTag, TagModel superTag, boolean countIfSame) {
        if (superTag == null)
            throw new IllegalArgumentException("Super tag param was null. Sub tag: " + subTag);

        if (subTag == null)
            throw new IllegalArgumentException("Sub tag param was null. Super tag: " + superTag);

        if (superTag.getName().equals(subTag.getName()))
            return true;

        Set<TagModel> walkedSet = new HashSet<>();

        Set<TagModel> currentSet = new HashSet<>();
        currentSet.add(subTag);

        do {
            walkedSet.addAll(currentSet);

            Set<TagModel> nextSet = new LinkedHashSet<>();
            for (TagModel currentTag : currentSet) {
                for (TagModel parent : currentTag.getDesignatedByTags()) {
                    if (superTag.equals(parent))
                        return true;
                    nextSet.add(parent);
                }
            }

            // Prevent infinite loops - detect graph cycles.
            Iterator<TagModel> it = walkedSet.iterator();
            while (it.hasNext()) {
                TagModel walkedTag = it.next();
                if (nextSet.contains(walkedTag))
                    nextSet.remove(walkedTag);
            }

            currentSet = nextSet;
        }
        while (!currentSet.isEmpty());
        return false;
    }


    /**
     * Returns a single parent of the given tag. If there are multiple parents, throws a WindupException.
     */
    public static TagModel getSingleParent(TagModel tag) {
        final Iterator<TagModel> parents = tag.getDesignatedByTags().iterator();
        if (!parents.hasNext())
            throw new WindupException("Tag is not designated by any tags: " + tag);

        final TagModel maybeOnlyParent = parents.next();

        if (parents.hasNext()) {
            StringBuilder sb = new StringBuilder();
            tag.getDesignatedByTags().iterator().forEachRemaining(x -> sb.append(x).append(", "));
            throw new WindupException(String.format("Tag %s is designated by multiple tags: %s", tag, sb.toString()));
        }

        return maybeOnlyParent;
    }
}
