package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import java.util.*;
import java.util.logging.Logger;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Returns a precomputed matrix - map of maps of maps of maps, boxTag -> rowTag -> project -> techName -> TechUsageStat.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      getTechnologiesIdentifiedForSubSectorAndRow(
 *          subsector: TagModel,
 *          row: TagModel,
 *          projectToCount: ProjectModel
 *      ):
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
    private TechReportService techReportService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.graphContext = event.getGraphContext();
        this.tagService = new TagGraphService(graphContext);
        this.techReportService = new TechReportService(graphContext);
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
            return TechReportService.queryMap(arguments);

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

        Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>> techStatsMap = techReportService.getTechStatsMap();

        ExecutionStatistics.get().end(NAME);
        return techStatsMap;
    }

}
