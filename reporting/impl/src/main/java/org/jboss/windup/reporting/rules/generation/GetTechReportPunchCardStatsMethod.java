package org.jboss.windup.reporting.rules.generation;

import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.freemarker.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportPunchCardModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import java.util.logging.Logger;
import org.jboss.windup.config.tags.TagService;
import org.jboss.windup.config.tags.TagServiceHolder;

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

        Map<ProjectModel, Map<String, Integer>> matrix = computeProjectAndTagsMatrix(this.graphContext);

        ExecutionStatistics.get().end(NAME);
        return matrix;
    }

    private Map<ProjectModel, Map<String, Integer>> computeProjectAndTagsMatrix(GraphContext grCtx) {
        // App -> tag name -> occurences.
        Map<ProjectModel, Map<String, Integer>> countsOfTagsInApps = new HashMap<>();

        // What sectors (column groups) and tech-groups (columns) should be on the report. View, Connect, Store, Sustain, ...
        GraphService<TagModel> service = new GraphService<>(grCtx, TagModel.class);
        TagModel sectorsHolderTag = service.getUniqueByProperty(TagModel.PROP_NAME, TechReportPunchCardModel.TAG_NAME_SECTORS);


        for (TagModel sectorTag : sectorsHolderTag.getDesignatedTags())
        {
            for (TagModel techTag : sectorTag.getDesignatedTags())
            {
                String tagName = techTag.getName();

                Map<ProjectModel, Integer> tagCountForAllApps = CreateTechReportPunchCardRuleProvider.getTagCountForAllApps(grCtx, tagName);

                // This transposes the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((project, count) -> {
                    Map<String, Integer> appTagCounts = countsOfTagsInApps.get(project);
                    if (null == appTagCounts)
                        countsOfTagsInApps.put(project, appTagCounts = new HashMap<>());
                    appTagCounts.put(tagName, count);
                });
            }
        }

        return countsOfTagsInApps;
    }

}
