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

        Set<TechnologyUsageStatisticsModel> techStats = getTechStats(boxTag, rowTag, projectModel);

        ExecutionStatistics.get().end(NAME);
        return techStats;
    }

    // TODO: This should be optimized by a precomputed matrix - map of maps of maps, boxTag -> rowTag -> project -> TechUsageStat.

    private Set<TechnologyUsageStatisticsModel> getTechStats(TagModel boxTag, TagModel rowTag, ProjectModel project)
    {
        final TagGraphService tagService = new TagGraphService(graphContext);

        final Iterable<TechnologyUsageStatisticsModel> statModels = graphContext.service(TechnologyUsageStatisticsModel.class).findAll();
        final Set<TechnologyUsageStatisticsModel> forGivenBoxAndRow = StreamSupport.stream(statModels.spliterator(), false)
                // Only given project.
                .filter(stat -> project == null || stat.getProjectModel() != null && stat.getProjectModel() == project)
                // Only those under both row and box tags.
                .filter(stat -> anyTagsUnderAllTags(tagService, stat.getTags(), Arrays.asList(new TagModel[]{boxTag, rowTag})))
                .collect(Collectors.toSet());
        return forGivenBoxAndRow;
    }

    /**
     * Returns whether the tags of given names are under all of the given tags (or same).
     */
    private boolean anyTagsUnderAllTags(TagGraphService tagService, Set<String> childTagNames, List<TagModel> maybeParentTags)
    {
        nextChild:
        for (String childTagName : childTagNames) {
            final TagModel childTag = tagService.getTagByName(childTagName);
            for (TagModel maybeParentTag : maybeParentTags)
                if (!tagService.isTagUnderTagOrSame(childTag, maybeParentTag))
                    continue nextChild;
            return true;
        }
        return false;
    }

}
