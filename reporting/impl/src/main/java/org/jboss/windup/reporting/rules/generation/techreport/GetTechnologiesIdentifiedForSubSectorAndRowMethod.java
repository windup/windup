package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.ExecutionStatistics;

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

        Map<String, TechReportService.TechUsageStatSum> techStats = getTechStats(boxTag, rowTag, projectModel);

        ExecutionStatistics.get().end(NAME);
        return techStats;
    }

    /**
     * This scans all {@link TechnologyUsageStatisticsModel}s and filters those belonging under given box/column and row, and project.
     *
     * @deprecated This is now optimized by a precomputed matrix - map of maps of maps, boxTag -> rowTag -> project -> techName -> TechUsageStat.
     *       See {@link SortTechUsageStatsMethod}.
     */
    private Map<String, TechReportService.TechUsageStatSum> getTechStats(TagModel boxTag, TagModel rowTag, ProjectModel project)
    {
        LOG.info(String.format("#### boxTag %s, rowTag %s, project %s", boxTag, rowTag, project));

        final TagGraphService tagService = new TagGraphService(graphContext);

        Map<String, TechReportService.TechUsageStatSum> sums = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        final Set<TechnologyUsageStatisticsModel> forGivenBoxAndRow = StreamSupport.stream(statModels.spliterator(), false)
                // Only the given project.
                .filter(stat -> project == null || stat.getProjectModel() != null && stat.getProjectModel().asVertex().getId() == project.asVertex().getId()) /// Can models use equals?
                .peek(stat -> LOG.info(String.format("    Checking '%s', so far %sx, tags: %s", stat.getName(), sums.getOrDefault(stat.getName(), new TechReportService.TechUsageStatSum("")).getOccurrenceCount(), stat.getTags())))
                // Only those under both row and box tags.
                .filter(stat -> {
                    final Set<String>[] normalAndSilly = TechReportService.splitSillyTagNames(graphContext, stat.getTags());

                    // Normal: any of the tags must fit into the given row and box/column,
                    if (anyTagsUnderAllTags(normalAndSilly[0], Arrays.asList(boxTag, rowTag)))
                        return true;

                    // Silly: there must be two silly labels representing row or box/column, or one label fitting the box name.
                    TechReportService.TechReportPlacement placement = TechReportService.processSillyLabels(graphContext, normalAndSilly[1]);
                    return placementBelongToThisBoxAndRow(placement, boxTag, rowTag);
                })
                .peek(stat -> {
                    LOG.info(String.format("    Summing '%s', so far %sx, tags: %s", stat.getName(), sums.getOrDefault(stat.getName(), new TechReportService.TechUsageStatSum("")).getOccurrenceCount(), stat.getTags()) );
                    sums.put(stat.getName(),
                            sums.getOrDefault(stat.getName(), new TechReportService.TechUsageStatSum(stat.getName())).add(stat) );
                })
                .collect(Collectors.toSet());

        return sums;
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
    private boolean placementBelongToThisBoxAndRow(TechReportService.TechReportPlacement placement, TagModel boxTag, TagModel rowTag)
    {
        return tagService.isTagUnderTagOrSame(placement.box, boxTag) && tagService.isTagUnderTagOrSame(placement.row, rowTag);
    }

}
