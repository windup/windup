package org.jboss.windup.reporting.rules.generation.techreport;

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
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        if (arguments.size() > 1) {
            throw new TemplateModelException("Expected 0 or 1 argument - project.");
        }

        // The project. May be null -> count from all applications.
        ProjectModel projectModel = null;
        if (arguments.size() > 0)
        {
            StringModel projectArg = (StringModel) arguments.get(2);
            if (null != projectArg)
                projectModel = (ProjectModel) projectArg.getWrappedObject();
        }

        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> techStatsMap = getTechStatsMap();

        ExecutionStatistics.get().end(NAME);
        return getTechStatsMap();
    }

    // Prepare a precomputed matrix - map of maps of maps: rowTag -> boxTag -> project -> silly label -> TechUsageStatSum.
    private Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> getTechStatsMap()
    {
        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        for (TechnologyUsageStatisticsModel statModel : statModels)
        {
            final Set<String>[] normalAndSilly = splitSillyTagNames(graphContext, statModel.getTags());
            final TechReportPlacement placement = processSillyLabels(graphContext, normalAndSilly[1]);

            // Sort them out to the map.
            final Map<String, Map<Long, Map<String, TechUsageStatSum>>> row = map.computeIfAbsent(placement.row.getName(), k -> new HashMap());
            final Map<Long, Map<String, TechUsageStatSum>> box = row.computeIfAbsent(placement.box.getName(), k -> new HashMap<>());

            final String statsModelLabel = statModel.getName();
            // All Projects
            final Map<String, TechUsageStatSum> statSumAll = box.computeIfAbsent(Long.valueOf(0), k -> new HashMap<>());
            statSumAll.put(statsModelLabel, new TechUsageStatSum(statsModelLabel));
            // Respective project
            final Long projectKey = (Long) statModel.getProjectModel().asVertex().getId();
            final Map<String, TechUsageStatSum> statSum = box.computeIfAbsent(projectKey, k -> new HashMap<>());
            statSum.put(statsModelLabel, new TechUsageStatSum(statsModelLabel));
        }
        return map;
    }
}
