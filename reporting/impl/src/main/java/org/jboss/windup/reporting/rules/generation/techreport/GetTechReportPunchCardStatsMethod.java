package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportPunchCardModel;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.TemplateModelException;
import java.util.logging.Logger;

/**
 * Gets the statistics of technology occurences per input application.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      getTechReportPunchCardStats( projectToCount: ProjectModel ): MatrixAndAggregated
 * </pre>
 *
 * <p> Returns a MatrixAndAggregated object, which holds:
 *      * A Map
 *         * key:   ApplicationProject vertex ID
 *         * value: Map
 *           * key:     tag name
 *           * value:   count of occurences of technologies bearing that tag and it's subtags.
 *      * A Map
 *         * key: ApplicationProject vertex ID
 *         * value: Map
 *           * key:   tag name
 *           * value: maximum count found in any input application. The largest number of values in the other map.
 *
 *      * A Map
 *         * key: ApplicationProject vertex ID
 *         * value: Map
 *           * key:   tag name
 *           * value: total count found in any input application. A sum of values in the other map.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class GetTechReportPunchCardStatsMethod implements WindupFreeMarkerMethod
{
    public static final Logger LOG = Logger.getLogger(GetTechReportPunchCardStatsMethod.class.getName());
    private static final String NAME = "getTechReportPunchCardStats";

    private GraphContext graphContext;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.graphContext = event.getGraphContext();
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ProjectModel.class.getSimpleName()
                    + " as a parameter and returns Map<Long, Integer> where the key is the effort level and the value is the number of incidents at that particular level of effort.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        ProjectModel projectModel = null;

        // The project. May be null -> all input applications.
        if (arguments.size() >= 1) {
            StringModel projectArg = (StringModel) arguments.get(0);
            projectModel = (ProjectModel) projectArg.getWrappedObject();
            throw new TemplateModelException("Project");
        }

        MatrixAndAggregated result = computeProjectAndTagsMatrix(this.graphContext, projectModel);

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    private MatrixAndAggregated computeProjectAndTagsMatrix(GraphContext grCtx, ProjectModel projectToCount)
    {
        // What sectors (column groups) and tech-groups (columns) should be on the report. View, Connect, Store, Sustain, ...
        GraphService<TagModel> service = new GraphService<>(grCtx, TagModel.class);
        TagModel sectorsHolderTag = service.getUniqueByProperty(TagModel.PROP_NAME, TechReportPunchCardModel.EDGE_TAG_SECTORS);
        if (null == sectorsHolderTag)
        {
            LOG.warning("Tech Report hierarchy definition TagModel not found, looked for tag name " + TechReportPunchCardModel.EDGE_TAG_SECTORS);
            return null;
        }

        // App -> tag name -> occurences.
        Map<Long, Map<String, Integer>> matrix = new HashMap<>();
        final Map<String, Integer> maximums = new HashMap<>();
        final Map<String, Integer> totals   = new HashMap<>();

        for (TagModel sectorTag : sectorsHolderTag.getDesignatedTags())
        {
            for (TagModel techTag : sectorTag.getDesignatedTags())
            {
                String tagName = techTag.getName();

                Map<Long, Integer> tagCountForAllApps = CreateTechReportPunchCardRuleProvider.getTagCountForAllApps(grCtx, tagName);

                // Transposes the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((project, count) -> {
                    Map<String, Integer> appTagCounts = matrix.computeIfAbsent(project, k -> new HashMap<>());
                    appTagCounts.put(tagName, count);

                    // Update tag's maximum.
                    maximums.put(tagName, Math.max(count, maximums.getOrDefault(tagName, 0)));
                    totals.put(tagName, count + totals.getOrDefault(tagName, 0));
                });
            }
        }

        final MatrixAndAggregated result = new MatrixAndAggregated(matrix, maximums, totals);
        return result;
    }


    /**
     * Just a structure to hold the method result.
     */
    public static class MatrixAndAggregated
    {
        private Map<Long, Map<String, Integer>> countsOfTagsInApps;
        private Map<String, Integer> maximumsPerTag;
        private Map<String, Integer> totalsPerTag;

        public MatrixAndAggregated(Map<Long, Map<String, Integer>> countsOfTagsInApps, Map<String, Integer> maximumsPerTag, Map<String, Integer> totalsPerTag)
        {
            this.countsOfTagsInApps = countsOfTagsInApps;
            this.maximumsPerTag = maximumsPerTag;
            this.totalsPerTag = totalsPerTag;
        }

        public Map<Long, Map<String, Integer>> getCountsOfTagsInApps() { return countsOfTagsInApps; }
        public Map<String, Integer> getMaximumsPerTag() { return maximumsPerTag; }
        public Map<String, Integer> getTotalsPerTag() { return totalsPerTag; }
    }
}
