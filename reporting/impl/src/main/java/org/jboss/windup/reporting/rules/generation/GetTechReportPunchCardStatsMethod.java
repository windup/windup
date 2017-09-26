package org.jboss.windup.reporting.rules.generation;

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
 * Gets the number of effort points involved in migrating this application.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      getTechReportPunchCardStats(): Map{app ProjectModel, Map{String tag, Integer count}}
 * </pre>
 *
 * <p> Returns
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
                    + " as a parameter and returns Map<Integer, Integer> where the key is the effort level and the value is the number of incidents at that particular level of effort.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);

        MatrixAndMaximums result = computeProjectAndTagsMatrix(this.graphContext);

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    private MatrixAndMaximums computeProjectAndTagsMatrix(GraphContext grCtx) {

        final MatrixAndMaximums result = new MatrixAndMaximums();

        // App -> tag name -> occurences.
        Map<ProjectModel, Map<String, Integer>> matrix = result.getCountsOfTagsInApps();
        final Map<String, Integer> maximums = result.getMaximumsPerTag();

        // What sectors (column groups) and tech-groups (columns) should be on the report. View, Connect, Store, Sustain, ...
        GraphService<TagModel> service = new GraphService<>(grCtx, TagModel.class);
        TagModel sectorsHolderTag = service.getUniqueByProperty(TagModel.PROP_NAME, TechReportPunchCardModel.TAG_NAME_SECTORS);
        if (null == sectorsHolderTag)
        {
            LOG.warning("Tech Report hierarchy definition TagModel not found, looked for tag name " + TechReportPunchCardModel.TAG_NAME_SECTORS);
            return null;
        }


        for (TagModel sectorTag : sectorsHolderTag.getDesignatedTags())
        {
            for (TagModel techTag : sectorTag.getDesignatedTags())
            {
                String tagName = techTag.getName();

                Map<ProjectModel, Integer> tagCountForAllApps = CreateTechReportPunchCardRuleProvider.getTagCountForAllApps(grCtx, tagName);

                // Transposes the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((project, count) -> {
                    Map<String, Integer> appTagCounts = matrix.computeIfAbsent(project, k -> new HashMap<>());
                    appTagCounts.put(tagName, count);

                    // Update tag's maximum.
                    maximums.put(tagName, count + maximums.getOrDefault(tagName, 0));
                });
            }
        }

        return result;
    }


    public static class MatrixAndMaximums
    {
        private Map<ProjectModel, Map<String, Integer>> countsOfTagsInApps = new HashMap<>();
        private Map<String, Integer> maximumsPerTag = new HashMap<>();

        public Map<ProjectModel, Map<String, Integer>> getCountsOfTagsInApps() { return countsOfTagsInApps; }
        public Map<String, Integer> getMaximumsPerTag() { return maximumsPerTag; }
    }
}
