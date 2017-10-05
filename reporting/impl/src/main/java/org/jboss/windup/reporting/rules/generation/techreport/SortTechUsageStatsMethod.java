package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
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
import static org.jboss.windup.reporting.rules.generation.techreport.GetTechnologiesIdentifiedForSubSectorAndRowMethod.*;
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
 *      ): List<{@link TechnologyUsageStatisticsModel}
 * </pre>
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SortTechUsageStatsMethod implements WindupFreeMarkerMethod
{
    public static final Logger LOG = Logger.getLogger(SortTechUsageStatsMethod.class.getName());
    private static final String NAME = "sortTechUsageStats";

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
        return "Sorts out the TechnologyUsageStatisticsModel-s into columns/boxes and rows defined by techReport-hierarchy.xml as per the tags and labels in the <technology-identified> operations.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() == 4)
            return queryMap(arguments);

        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        if (arguments.size() > 1)
            throw new TemplateModelException("Expected 0 or 1 argument - project.");

        // The project. May be null -> count from all applications.
        // TODO Not used yet.
        ProjectModel projectModel = null;
        if (arguments.size() == 1)
        {
            StringModel projectArg = (StringModel) arguments.get(0);
            if (null != projectArg)
                projectModel = (ProjectModel) projectArg.getWrappedObject();
        }

        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> techStatsMap = getTechStatsMap();

        ExecutionStatistics.get().end(NAME);
        return techStatsMap;
    }

    /**
     * Prepares a precomputed matrix - map of maps of maps: rowTag -> boxTag -> project -> silly label -> TechUsageStatSum.
     */
    private Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> getTechStatsMap()
    {
        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        for (TechnologyUsageStatisticsModel stat : statModels)
        {
            LOG.info(String.format("    Rolling up '%s', count: %sx, tags: %s", stat.getName(), stat.getOccurrenceCount(), stat.getTags()) );

            final Set<String>[] normalAndSilly = splitSillyTagNames(graphContext, stat.getTags());
            TechReportPlacement placement = processSillyLabels(graphContext, normalAndSilly[1]);
            placement = normalizeSillyPlacement(graphContext, placement);

            // Sort them out to the map.
            final Map<String, Map<Long, Map<String, TechUsageStatSum>>> row = map.computeIfAbsent(placement.row.getName(), k -> new HashMap());
            final Map<Long, Map<String, TechUsageStatSum>> box = row.computeIfAbsent(placement.box.getName(), k -> new HashMap<>());

            final String statsModelLabel = stat.getName();
            // All Projects
            final Map<String, TechUsageStatSum> statSumAll = box.computeIfAbsent(Long.valueOf(0), k -> new HashMap<>());
            statSumAll.put(statsModelLabel, new TechUsageStatSum(stat));
            // Respective project
            final Long projectKey = (Long) stat.getProjectModel().asVertex().getId();
            final Map<String, TechUsageStatSum> statSum = box.computeIfAbsent(projectKey, k -> new HashMap<>());
            statSum.put(statsModelLabel, new TechUsageStatSum(stat));
        }
        return map;
    }

    /**
     * A helper method to query the structure created above, since Freemarker can't query maps with numerical keys.
     */
    private Map<String, TechUsageStatSum> queryMap(List arguments)
    {
        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = (Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>>) ((StringModel)arguments.get(0)).getWrappedObject();
        String rowTagName = ((SimpleScalar) arguments.get(1)).getAsString();
        String boxTagName = ((SimpleScalar) arguments.get(2)).getAsString();
        Long projectId = ((SimpleNumber) arguments.get(3)).getAsNumber().longValue();

        final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowMap = map.get(rowTagName);
        if (null == rowMap)
            return null;
        final Map<Long, Map<String, TechUsageStatSum>> boxMap = rowMap.get(boxTagName);
        if (null == boxMap)
            return null;
        final Map<String, TechUsageStatSum> projectMap = boxMap.get(projectId);
        return projectMap;
    }

}
