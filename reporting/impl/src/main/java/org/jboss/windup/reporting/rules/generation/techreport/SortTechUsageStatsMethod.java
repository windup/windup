package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

/**
 * Returns a precomputed matrix - map of maps of maps of maps, boxTag -> rowTag -> project -> techName -> TechUsageStat.
 *
 * <p> Called from a Freemarker template as follows:
 *
 * <pre>
 *      sortTechUsageStats( projectToCount: ProjectModel ):
 *          Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>>
 * </pre>
 *
 * If given 4 parameters, this function queries the structure returned by it as described above.
 * The 4 parameters then are:
 *     Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> - the map described above,
 *     String - row tag name
 *     String - box tag name
 *     Long   - project (vertex) ID
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class SortTechUsageStatsMethod implements WindupFreeMarkerMethod
{
    public static final Logger LOG = Logger.getLogger(SortTechUsageStatsMethod.class.getName());
    private static final String NAME = "sortTechUsageStats";

    private GraphContext graphContext;
    private TechReportService techReportService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.graphContext = event.getGraphContext();
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
            return tryQueryMap(arguments);

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

        Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>> techStatsMap = techReportService.getTechStatsMap(projectModel);

        ExecutionStatistics.get().end(NAME);
        return techStatsMap;
    }

    private Object tryQueryMap(List arguments)
    {
        try
        {
            Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>> map =
                    (Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>>) ((StringModel) arguments.get(0)).getWrappedObject();
            String rowTagName = ((SimpleScalar) arguments.get(1)).getAsString();
            String boxTagName = ((SimpleScalar) arguments.get(2)).getAsString();
            Long projectId = ((SimpleNumber) arguments.get(3)).getAsNumber().longValue();
            return TechReportService.queryMap(map, rowTagName, boxTagName, projectId);
        }
        catch (Exception ex)
        {
            throw new WindupException(String.format("Wrong parameters to query the map, should be: map, string, string, number.\n\tWas: %s%s%s%s",
                    arguments.get(0).getClass(), arguments.get(1).getClass(), arguments.get(2).getClass(), arguments.get(3).getClass() ), ex);
        }
    }

}
