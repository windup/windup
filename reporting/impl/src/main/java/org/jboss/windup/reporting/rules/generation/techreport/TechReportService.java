package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.*;
import java.util.logging.Logger;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.exception.WindupException;

public class TechReportService
{
    public static final Logger LOG = Logger.getLogger(TechReportService.class.getName());

    private final TagGraphService tagService;
    private final GraphContext graphContext;

    public TechReportService(GraphContext graphContext)
    {
        this.graphContext = graphContext;
        this.tagService = new TagGraphService(graphContext);
    }



    /**
     * Prepares a precomputed matrix - map of maps of maps: rowTag -> boxTag -> project -> silly label -> TechUsageStatSum.
     * @param onlyForProject Sum the statistics only for this project.
     */
    Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> getTechStatsMap(ProjectModel onlyForProject)
    {
        final Long onlyID = onlyForProject == null ? null : (Long) onlyForProject.getRootProjectModel().asVertex().getId();
        LOG.info(String.format("### Creating tech stats map for " + (onlyForProject == null ? "global report" : "project #d"), onlyID));

        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        for (TechnologyUsageStatisticsModel stat : statModels)
        {
            final Long projectKey = (Long) stat.getProjectModel().getRootProjectModel().asVertex().getId();

            LOG.info(String.format("--- Counting up p#%d '%s', count: %sx, tags: %s", projectKey, stat.getName(), stat.getOccurrenceCount(), stat.getTags()) );
            //if (onlyForProject != null)
            //    LOG.info(String.format("--- Project:  - %s", projectKey, stat.getProjectModel().getRootProjectModel().getName()));

            if (onlyForProject != null && !projectKey.equals(onlyForProject.getRootProjectModel().asVertex().getId())) {
                LOG.info("\t\tThis stat is for other project, skipping.");
                continue;
            }

            // Identify placement
            final Set<String>[] normalAndSilly = TechReportService.splitSillyTagNames(graphContext, stat.getTags());
            TechReportService.TechReportPlacement placement = TechReportService.processSillyLabels(graphContext, normalAndSilly[1]);
            if (placement.box == null || placement.row == null)
            {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }

            // Normalize placement
            placement = TechReportService.normalizeSillyPlacement(graphContext, placement);

            LOG.info(String.format("\tplacement: %s, projectKey: %d", placement, projectKey)); ///
            if (placement.box == null || placement.row == null)
            {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }


            // For boxes report - show each tech in sector, row, box. For individual projects and sum for all projects.
            mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), projectKey, stat.getName(), stat, false);
            mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), 0L, stat.getName(), stat, false);

            // For the punch card report - roll up rows and individual techs.
            mergeToTheRightCell(map, "", placement.box.getName(), projectKey, "", stat, false);

            // For the punch card report - maximum count for each box.
            mergeToTheRightCell(map, "", placement.box.getName(), 0L, "", stat, true);
        }
        return map;
    }

    private static void mergeToTheRightCell(
            Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> matrix,
            String rowName,
            String boxName,
            Long projectKey,
            String techLabel,
            TechnologyUsageStatisticsModel stat, boolean maxInsteadOfAdd)
    {
        final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowAll = matrix.computeIfAbsent(rowName, k -> new HashMap());
        final Map<Long, Map<String, TechUsageStatSum>> boxAll = rowAll.computeIfAbsent(boxName, k -> new HashMap<>());
        final Map<String, TechUsageStatSum> statSum = boxAll.computeIfAbsent(projectKey, k -> new HashMap<>());
        final TechUsageStatSum techUsageStatSum = statSum.computeIfAbsent(techLabel, k -> new TechUsageStatSum(techLabel));
        if (maxInsteadOfAdd)
            techUsageStatSum.max(stat);
        else
            techUsageStatSum.add(stat);
    }

    /**
     * A helper method to query the structure created by getTechStatsMap, to overcome some Freemarker Map syntax limitations.
     */
    static Map<String, TechUsageStatSum> queryMap(Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map, String rowTagName, String boxTagName, Long projectId)
    {
        final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowMap = map.get(rowTagName);
        if (null == rowMap)
            return null;
        final Map<Long, Map<String, TechUsageStatSum>> boxMap = rowMap.get(boxTagName);
        if (null == boxMap)
            return null;
        final Map<String, TechUsageStatSum> projectMap = boxMap.get(projectId);
        return projectMap;
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

        potentialSillyTags.forEach(name -> {
            final TagModel sillyTag = tagService.getTagByName("silly:" + Tag.normalizeName(name));
            if (null != sillyTag)
                sillyNames.add(sillyTag.getName());
            else
                // Some may be undefined. This will happen when someone attempts to add a new sector, row or column/box.
                normalNames.add(name);
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
            GetTechnologiesIdentifiedForBoxAndRowMethod.LOG.severe("There should always be exactly 3 silly labels - row, sector, column/box. It was: " + tagNames);

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

        LOG.info(String.format("\t\tLabels %s identified as: sector: %s, box: %s, row: %s", tagNames, placement.sector, placement.box, placement.row));
        if (placement.box == null || placement.row == null)
        {
            GetTechnologiesIdentifiedForBoxAndRowMethod.LOG.severe(String.format("There should always be exactly 3 silly labels - row, sector, column/box. Found: %s, of which box: %s, row: %s", tagNames, placement.box, placement.row));
        }
        return placement;
    }

    /**
     * This relies on the tag structure in the XML when the silly mapping tags have exactly one parent
     * outside the silly group, which is the tag they are mapped to.
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
        if (tag == null)
            return null;

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

        public TechUsageStatSum max(TechnologyUsageStatisticsModel stat)
        {
            this.count = Math.max(count, stat.getOccurrenceCount());
            return this;
        }

        public TechUsageStatSum add(TechnologyUsageStatisticsModel stat)
        {
            if (!"".equals(this.name) && !this.name.equals(stat.getName()))
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public TechUsageStatSum add(TechUsageStatSum stat)
        {
            if (!"".equals(this.name) && !this.name.equals(stat.getName()))
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

        @Override
        public String toString() {
            return "{" + name + " " + count + "×, [" + tags + "]}";
        }
    }
}
