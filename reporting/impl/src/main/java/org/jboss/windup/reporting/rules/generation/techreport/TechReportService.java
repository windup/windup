package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.exception.WindupException;

public class TechReportService
{
    private final TagGraphService tagService;
    private GraphContext graphContext;

    public TechReportService(GraphContext graphContext)
    {
        this.graphContext = graphContext;
        this.tagService = new TagGraphService(graphContext);
    }


    /**
     * Translates the silly tags (labels) to their normalized real tag counterparts.
     * Returns a 2-item array; index 0 has the normal names, index 1 the silly names.
     *
     * Due to bad design, the rules contain column and row titles for the graph, rather than technology tags.
     * To make them fit into the tag system, this translation is needed. See also the "silly:..." tags in the report hierarchy definition.
     */
    static Set<String>[] splitSillyTagNames(GraphContext graphContext, Set<String> potentialSillyTags)
    {
        final TagGraphService tagService = new TagGraphService(graphContext);

        Set<String> normalNames = new HashSet<>();
        Set<String> sillyNames = new HashSet<>();

        potentialSillyTags.stream().forEach(name -> {
            final TagModel sillyTag = tagService.getTagByName("silly:" + Tag.normalizeName(name));
            if (null != sillyTag)
                sillyNames.add(sillyTag.getName());
            else
                // Some may be undefined. This will happen when someone attempts to add a new sector, row or column/box.
                normalNames.add(sillyTag.getName());
        });
        return new Set[]{normalNames, sillyNames};
    }

    /**
     * From three tagNames, if one is under sectorTag and one under rowTag, returns the remaining one, which is supposedly the a box label.
     * Otherwise, returns null.
     */
    static TechReportPlacement processSillyLabels(GraphContext grCtx, Set<String> tagNames)
    {
        TagGraphService tagService = new TagGraphService(grCtx);

        if (tagNames.size() < 3)
            throw new WindupException("There should always be exactly 3 silly labels - row, sector, column/box. It was: " + tagNames);
        if (tagNames.size() > 3)
            GetTechnologiesIdentifiedForSubSectorAndRowMethod.LOG.severe("There should always be exactly 3 silly labels - row, sector, column/box. It was: " + tagNames);

        TechReportPlacement placement = new TechReportPlacement();

        final TagModel sillySectorsTag = tagService.getTagByName("techReport:sillySectors");
        final TagModel sillyBoxesTag = tagService.getTagByName("techReport:sillyBoxes");
        final TagModel sillyRowsTag = tagService.getTagByName("techReport:sillyRows");


        Set<String> tagNames2 = new HashSet(tagNames);
        for (Iterator<String> tagNamesIt = tagNames2.iterator(); tagNamesIt.hasNext(); )
        {
            String name = tagNamesIt.next();
            final TagModel tag = tagService.getTagByName(name);
            if (null == tag)
                continue;

            if (tagService.isTagUnderTagOrSame(tag, sillySectorsTag))
            {
                placement.sector = tag;
                tagNamesIt.remove();
            }
            else if (tagService.isTagUnderTagOrSame(tag, sillyBoxesTag))
            {
                placement.box = tag;
                tagNamesIt.remove();
            }
            else if (tagService.isTagUnderTagOrSame(tag, sillyRowsTag))
            {
                placement.row = tag;
                tagNamesIt.remove();
            }
        }
        placement.unknown = tagNames2;

        GetTechnologiesIdentifiedForSubSectorAndRowMethod.LOG.info(String.format("\t\tLabels %s identified as: sector: %s, box: %s, row: %s", tagNames, placement.sector, placement.box, placement.row));
        if (placement.box == null || placement.row == null)
        {
            GetTechnologiesIdentifiedForSubSectorAndRowMethod.LOG.severe(String.format("There should always be exactly 3 silly labels - row, sector, column/box. Found: %s, of which box: %s, row: %s", tagNames, placement.box, placement.row));
        }
        return placement;
    }

    /**
     * This relies on the tag structure in the XML when the silly mapping tags have exactly one parent outside the silly group, which is the tag they are mapped to.
     */
    static TechReportPlacement normalizeSillyPlacement(GraphContext grCtx, TechReportPlacement sillyPlacement)
    {
        TagGraphService tagService = new TagGraphService(grCtx);

        final TechReportPlacement normalPlacement = new TechReportPlacement();
        normalPlacement.sector = getNonSillyParent(tagService, sillyPlacement.sector);
        normalPlacement.box = getNonSillyParent(tagService, sillyPlacement.box);
        normalPlacement.row = getNonSillyParent(tagService, sillyPlacement.row);
        return normalPlacement;
    }

    private static TagModel getNonSillyParent(TagGraphService tagService, TagModel tag)
    {
        final TagModel sillyRoot = tagService.getTagByName("techReport:mappingOfSillyTagNames");

        final Iterator<TagModel> parents = tag.getDesignatedByTags().iterator();
        if (!parents.hasNext())
            throw new WindupException("Tag is not designated by any tags: " + tag);

        TagModel nonSillyParent = null;
        do {
            TagModel parentTag = parents.next();
            if (tagService.isTagUnderTagOrSame(parentTag, sillyRoot))
                continue;
            if (nonSillyParent != null)
                throw new WindupException(String.format("Tag %s has more than one non-silly parent: %s, %s", nonSillyParent, parentTag));
            nonSillyParent = parentTag;
        }
        while (parents.hasNext());

        return nonSillyParent;
    }


    /**
     * A placement of a technology in a tech report.
     * Boxes in grid report == columns in punch card report.
     */
    public static class TechReportPlacement {
        public TagModel sector;
        public TagModel box;
        public TagModel row;
        public Set<String> unknown;

        @Override
        public String toString()
        {
            return "TechReportPlacement{sector=" + sector + ", box=" + box + ", row=" + row + ", unknown=" + unknown + '}';
        }
    }

    /**
     * Keeps the aggregated data from multiple {@link TechnologyUsageStatisticsModel}s.
     */
    public static class TechUsageStatSum {
        String name;
        int count = 0;
        Set<String> tags = new HashSet<>();

        public TechUsageStatSum(String name)
        {
            this.name = name;
        }

        public TechUsageStatSum(TechnologyUsageStatisticsModel stat)
        {
            this.name = stat.getName();
            this.count = stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
        }

        public TechUsageStatSum add(TechnologyUsageStatisticsModel stat)
        {
            if (!this.name.equals(stat.getName()))
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public TechUsageStatSum add(TechUsageStatSum stat)
        {
            if (!this.name.equals(stat.getName()))
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public String getName()
        {
            return name;
        }

        public int getOccurrenceCount()
        {
            return count;
        }

        public Set<String> getTags()
        {
            return tags;
        }
    }
}
