package org.jboss.windup.reporting.rules.generation.techreport;

import com.tinkerpop.blueprints.Graph;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

/**
 * Gets the list of TechnologyUsageStatisticsModel-s which should be displayed in the box given by the report "coordinates" tags (subsector/box, row).
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      getTechnologiesIdentifiedForSubSectorAndRow(
 *          subsector: TagModel,
 *          row: TagModel,
 *          projectToCount: ProjectModel
 *      ): List<{@link org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel}
 * </pre>
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class GetTechnologiesIdentifiedForSubSectorAndRowMethod implements WindupFreeMarkerMethod
{
    public static final Logger LOG = Logger.getLogger(GetTechnologiesIdentifiedForSubSectorAndRowMethod.class.getName());
    private static final String NAME = "getTechnologiesIdentifiedForSubSectorAndRow";

    private GraphContext graphContext;
    private TagGraphService tagService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.graphContext = event.getGraphContext();
        this.tagService = new TagGraphService(graphContext);
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        if (arguments.size() < 2) {
            throw new TemplateModelException("Expected 2 or 3 arguments - a subsector tag, a row tag and optionally, a project.");
        }

        StringModel boxArg = (StringModel) arguments.get(0);
        TagModel boxTag = (TagModel) boxArg.getWrappedObject();

        StringModel rowArg = (StringModel) arguments.get(1);
        TagModel rowTag = (TagModel) rowArg.getWrappedObject();

        // The project. May be null -> count from all applications.
        ProjectModel projectModel = null;
        if (arguments.size() >= 3)
        {
            StringModel projectArg = (StringModel) arguments.get(2);
            if (null != projectArg)
                projectModel = (ProjectModel) projectArg.getWrappedObject();
        }

        Map<String, TechUsageStatSum> techStats = getTechStats(boxTag, rowTag, projectModel);

        ExecutionStatistics.get().end(NAME);
        return techStats;
    }

    // TODO: This should be optimized by a precomputed matrix - map of maps of maps, boxTag -> rowTag -> project -> TechUsageStat.

    /**
     * This scans all {@link TechnologyUsageStatisticsModel}s and filters those belonging under given box/column and row, and project.
     */
    private Map<String, TechUsageStatSum> getTechStats(TagModel boxTag, TagModel rowTag, ProjectModel project)
    {
        LOG.info(String.format("#### boxTag %s, rowTag %s, project %s", boxTag, rowTag, project));

        final TagGraphService tagService = new TagGraphService(graphContext);

        Map<String, TechUsageStatSum> sums = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        final Set<TechnologyUsageStatisticsModel> forGivenBoxAndRow = StreamSupport.stream(statModels.spliterator(), false)
                // Only the given project.
                .filter(stat -> project == null || stat.getProjectModel() != null && stat.getProjectModel().asVertex().getId() == project.asVertex().getId()) /// Can models use equals?
                .peek(stat -> LOG.info(String.format("    Checking '%s', so far %sx, tags: %s", stat.getName(), sums.getOrDefault(stat.getName(), new TechUsageStatSum("")).getOccurrenceCount(), stat.getTags())))
                // Only those under both row and box tags.
                .filter(stat -> {
                    final Set<String>[] normalAndSilly = splitSillyTagNames(graphContext, stat.getTags());

                    // Normal: any of the tags must fit into the given row and box/column,
                    if (anyTagsUnderAllTags(normalAndSilly[0], Arrays.asList(boxTag, rowTag)))
                        return true;

                    // Silly: there must be two silly labels representing row or box/column, or one label fitting the box name.
                    TechReportPlacement placement = processSillyLabels(graphContext, normalAndSilly[1]);
                    //return placement.box != null || (placement.sector != null && placement.box != null);
                    return placementBelongToThisBoxAndRow(placement, boxTag, rowTag);
                })
                .peek(stat -> {
                    LOG.info(String.format("    Summing '%s', so far %sx, tags: %s", stat.getName(), sums.getOrDefault(stat.getName(), new TechUsageStatSum("")).getOccurrenceCount(), stat.getTags()) );
                    //sums.merge(stat.getName(), new TechUsageStatSum(stat), (sumFromMap, sumNext) -> sumFromMap.add(sumNext))///
                    sums.put(stat.getName(),
                            sums.getOrDefault(stat.getName(), new TechUsageStatSum(stat.getName())).add(stat) );
                })
                .collect(Collectors.toSet());

        //return forGivenBoxAndRow;
        return sums;
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
     * Returns whether the tags of given names are under all of the given tags (or same).
     */
    private boolean anyTagsUnderAllTags(Set<String> childTagNames, List<TagModel> maybeParentTags)
    {
        nextChild:
        for (String childTagName : childTagNames) {
            final TagModel childTag = tagService.getTagByName(childTagName);
            if (null == childTag)
            {
                LOG.warning("        Undefined tag used in identified technology tags, will not fit into any tech report box: " + childTagName);
                continue;
            }

            for (TagModel maybeParentTag : maybeParentTags)
            {
                LOG.info(String.format("        Trying, subTag: name %s -> %s, maybeParent: %s", childTagName, childTag, maybeParentTag) );///
                if (!tagService.isTagUnderTagOrSame(childTag, maybeParentTag))
                    continue nextChild;
                LOG.info("          --> YEP :)");
            }
            return true;
        }
        return false;
    }

    /**
     * Returns whether out of three tags, one is under sectorTag and one under rowTag. The remaining one is supposedly the a box label.
     */
    boolean placementBelongToThisBoxAndRow(TechReportPlacement placement, TagModel boxTag, TagModel rowTag)
    {
        return tagService.isTagUnderTagOrSame(placement.box, boxTag) && tagService.isTagUnderTagOrSame(placement.row, rowTag);
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
            LOG.severe("There should always be exactly 3 silly labels - row, sector, column/box. It was: " + tagNames);

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
            LOG.severe(String.format("There should always be exactly 3 silly labels - row, sector, column/box. Found: %s, of which box: %s, row: %s", tagNames, placement.box, placement.row));
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
