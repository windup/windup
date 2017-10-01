package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationInputPathModel;
import org.jboss.windup.graph.model.ApplicationProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.*;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.graph.model.ProjectModel;

/**
 * Creates the ReportModel for Tech stats report, and the data structure the template needs.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateTechReportPunchCardRuleProvider extends AbstractRuleProvider
{
    public static final Logger LOG = Logger.getLogger(CreateTechReportPunchCardRuleProvider.class.getName());


    public static final String TEMPLATE_PATH_PUNCH = "/reports/templates/techReport-punchCard.ftl";
    public static final String REPORT_NAME_PUNCH = "Technologies";
    public static final String REPORT_DESCRIPTION_PUNCH =
            "This report is a statistic of technologies occurences in the input applications."
            + " It shows how the technologies are distributed and is mostly useful when analysing many applications.";

    public static final String TEMPLATE_PATH_BOXES = "/reports/templates/techReport-boxes.ftl";
    private static final String REPORT_NAME_BOXES = "Technologies boxes";
    public static final String REPORT_DESCRIPTION_BOXES =
            "This report is a statistic of technologies occurences in the input applications."
                    + " It is an overview of what techogies are found in given project or a set of projects.";



    @Inject private TagServiceHolder tagServiceHolder;

    @Override
    public void put(Object key, Object value)
    {
        super.put(key, value);
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            /// TODO: Move this to a FeedTagStructureToGraphRuleProvider.
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    ///LOG.info("TagServiceHolder = " + tagServiceHolder);
                    new TagGraphService(event.getGraphContext()).feedTheWholeTagStructureToGraph(tagServiceHolder.getTagService());
                    CreateTechReportPunchCardRuleProvider.this.put(TagServiceHolder.class, tagServiceHolder);
                }
            })

            /// TODO: Move this to a MarkApplicationProjectModels.
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    event.getGraphContext().service(ApplicationInputPathModel.class).findAll()
                        .forEach(path -> GraphService.addTypeToModel(event.getGraphContext(), path.getProjectModel(), ApplicationProjectModel.class));
                }
            })


            .addRule()
            .perform(new CreateTechReportPunchCardOperation());
    }

    private class CreateTechReportPunchCardOperation extends GraphOperation
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext evCtx)
        {
            GraphContext grCtx = event.getGraphContext();

            listAllTechUsageStats(grCtx);
            listAllApplicationModels(grCtx);

            // Create the report model.
            TechReportPunchCardModel reportPunch = createTechReportPunchCard(grCtx);
            TechReportPunchCardModel reportBoxes = createTechReportBoxes(grCtx);

            // Add sectors and rows to them.
            TagGraphService tagGraphService = new TagGraphService(grCtx);
            TagModel sectorsTag = tagGraphService.getTagByName(TechReportPunchCardModel.EDGE_TAG_SECTORS);
            TagModel rowsTag = tagGraphService.getTagByName(TechReportPunchCardModel.EDGE_TAG_ROWS);
            if (null == sectorsTag)
                throw new WindupException("Tech report sectors tag, '" + TechReportPunchCardModel.EDGE_TAG_SECTORS + "', not found.");
            if (null == rowsTag)
                throw new WindupException("Tech report rows tag, '" + TechReportPunchCardModel.EDGE_TAG_ROWS + "', not found.");
            reportPunch.setSectorsHolderTag(sectorsTag);
            reportBoxes.setSectorsHolderTag(sectorsTag);
            reportBoxes.setRowsHolderTag(rowsTag);

            // Now let's fill it with data.
            /* This is not used, it's computed by GetTechReportPunchCardStatsMethod.
            Map<Long, Map<String, Integer>> countsOfTagsInApps = computeProjectAndTagsMatrix(grCtx);

            // Find maximum number of occurences within the apps. Used for cirle size.
            Map<String, Integer> maximumsPerTech = computeMaxCountPerTag(countsOfTagsInApps);
            reportPunch.setMaximumCounts(maximumsPerTech);
            */

            // TODO: Maybe it would be better to query like this?
            // For each application,
            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(grCtx).getInputPaths())
            {
                List types = (List)inputPath.asVertex().getProperty(WindupVertexFrame.TYPE_PROP);
                LOG.info("InputPath type:" + types.toString());
            }
        }

        private Map<String,Integer> computeMaxCountPerTag(Map<Long, Map<String, Integer>> countsOfTagsInApps)
        {
            final HashMap<String, Integer> maxCountPerTag = new HashMap<>();
            for (Map<String, Integer> countsOfTechs : countsOfTagsInApps.values())
            {
                countsOfTechs.forEach((techName, count) -> {
                    int current = maxCountPerTag.getOrDefault(techName, 0);
                    maxCountPerTag.put(techName, Math.max(count, current));
                });
            }
            return maxCountPerTag;
        }

        private TechReportPunchCardModel createTechReportPunchCard(GraphContext grCtx){
            TechReportPunchCardModel report = createTechReportBase(grCtx, "punch");
            report.setReportName(REPORT_NAME_PUNCH);
            report.setTemplatePath(TEMPLATE_PATH_PUNCH);
            report.setDescription(REPORT_DESCRIPTION_PUNCH);
            report.setReportIconClass("glyphicon glyphicon-tags");

            return report;
        }
        private TechReportPunchCardModel createTechReportBoxes(GraphContext grCtx){
            TechReportPunchCardModel report = createTechReportBase(grCtx, "boxes");
            report.setReportName(REPORT_NAME_BOXES);
            report.setTemplatePath(TEMPLATE_PATH_BOXES);
            report.setDescription(REPORT_DESCRIPTION_BOXES);
            report.setReportIconClass("glyphicon glyphicon-tags");

            return report;
        }
        private TechReportPunchCardModel createTechReportBase(GraphContext grCtx, String reportIdentifier)
        {
            ApplicationReportService applicationReportService = new ApplicationReportService(grCtx);
            ApplicationReportModel report = applicationReportService.create();
            report.setTemplateType(TemplateType.FREEMARKER);
            report.setDisplayInApplicationReportIndex(true);
            report.setDisplayInGlobalApplicationIndex(true);
            report.setReportPriority(101);

            ReportService reportService = new ReportService(grCtx);
            reportService.setUniqueFilename(report, "techReport-" + reportIdentifier, "html");

            TechReportPunchCardModel techReport = new GraphService<>(grCtx, TechReportPunchCardModel.class).addTypeToModel(report);
            return techReport;
        }
    }


    /*
        Needs TagService, which I don't know how to get from a Freemarker method.
        TODO: Maybe kick this and use only GetTechReportPunchCardStatsMethod#computeProjectAndTagsMatrix()?
     */
    private Map<Long, Map<String, Integer>> computeProjectAndTagsMatrix(GraphContext grCtx) {
        // App -> tag name -> occurences.
        Map<Long, Map<String, Integer>> countsOfTagsInApps = new HashMap<>();
        Map<String, Integer> maxCountPerTag = new HashMap<>();

        // What sectors (column groups) and sub-sectors (columns) should be on the report. View, Connect, Store, Sustain, ...
        Tag sectorsTag = tagServiceHolder.getTagService().getTag(TechReportPunchCardModel.EDGE_TAG_SECTORS);
        if (null == sectorsTag)
            throw new WindupException("Tech report hierarchy definition tag, '"+TechReportPunchCardModel.EDGE_TAG_SECTORS +"', not found.");

        // For each sector / subsector
        for (Tag tag1 : sectorsTag.getContainedTags())
        {
            for (Tag tag2 : tag1.getContainedTags())
            {
                String tagName = tag2.getName();
                Map<Long, Integer> tagCountForAllApps = getTagCountForAllApps(grCtx, tagName);
                LOG.info("Computed tag " + tagName + ":\n" + Logging.printMap(tagCountForAllApps, true));///

                // Transpose the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((projectVertexId, count) -> {
                    Map<String, Integer> appTagCounts = countsOfTagsInApps.computeIfAbsent(projectVertexId, k -> new HashMap<>());
                    appTagCounts.put(tagName, count);
                });
            }
        }

        return countsOfTagsInApps;
    }


    /**
     * @return Map of counts of given tag and subtags occurrences in all input applications.
     *         I.e. how many items tagged with any tag under subSectorTag are there in each input application.
     *         The key is the vertex ID.
     */
    public static Map<Long, Integer> getTagCountForAllApps(GraphContext grCtx, String subSectorTagName)
    {
        // Get all "subtags" of this tag.
        //Set<String> subTagsNames = getSubTagNames_tagService(subSectorTagName);
        Set<String> subTagsNames = getSubTagNames_graph(grCtx, subSectorTagName);

        // Get all apps.
        Set<ApplicationProjectModel> apps = getAllApplications(grCtx);

        Map<Long, Integer> appToTechSectorCoveredTagsOccurrenceCount = new HashMap<>();

        for (ProjectModel app : apps)
        {
            int countSoFar = 0;
            // Get the TechnologyUsageStatisticsModel's for this ProjectModel
            Iterable<Vertex> statsIt = app.asVertex().getVertices(Direction.IN, TechnologyUsageStatisticsModel.PROJECT_MODEL);
            for (Vertex vStat : statsIt)
            {
                TechnologyUsageStatisticsModel stat = grCtx.getFramed().frame(vStat, TechnologyUsageStatisticsModel.class);

                // Tags of this TechUsageStat covered by this sector Tag.
                Set<String> techStatTagsCoveredByGivenTag = stat.getTags().stream().filter(name -> subTagsNames.contains(name)).collect(Collectors.toSet());
                // TODO: Optimize this when proven stable - sum the number in the stream
                //boolean covered = stat.getTags().stream().anyMatch(name -> subTagsNames.contains(name));
                if (!techStatTagsCoveredByGivenTag.isEmpty())
                    countSoFar += stat.getOccurrenceCount();
            }
            appToTechSectorCoveredTagsOccurrenceCount.put((Long)app.asVertex().getId(), countSoFar);
        }
        return appToTechSectorCoveredTagsOccurrenceCount;
    }

    private static Set<String> getSubTagNames_graph(GraphContext grCtx, String subSectorTagName)
    {
        TagGraphService tagService = new TagGraphService(grCtx);
        Set<TagModel> subTags = tagService.getDescendantTags(tagService.getTagByName(subSectorTagName));
        return subTags.stream().map(t->t.getName()).collect(Collectors.toSet());
    }


    /**
     * Returns all ApplicationProjectModels.
     */
    private static Set<ApplicationProjectModel> getAllApplications(GraphContext grCtx)
    {
        Set<ApplicationProjectModel> apps = new HashSet<>();
        Iterable<ApplicationProjectModel> appProjects = grCtx.findAll(ApplicationProjectModel.class);
        for (ApplicationProjectModel appProject : appProjects)
            apps.add(appProject);
        return apps;
    }



    /**
     * @return Map of counts of given tag occurrences in all input applications.
     *
     * TODO This is inconvenient as the template needs things grouped by app, not by technology...
     * FIXME Due to the mismatch between how the TechUsageStats was expected to work and how it works, this approach is not possible - doesn't cover the subtags.
     */
    private static Map<ProjectModel, Integer> getTagCountForAllApps_nonDeep(GraphContext grCtx, String subSectorTagName) {
        final GraphService<TechnologyUsageStatisticsModel> techUsageService = new GraphService<>(grCtx, TechnologyUsageStatisticsModel.class);
        Iterable<TechnologyUsageStatisticsModel> usageStats = techUsageService.findAllByProperty(TechnologyUsageStatisticsModel.NAME, subSectorTagName);

        Map<ProjectModel, Integer> tagsInProject = new HashMap();
        for (TechnologyUsageStatisticsModel stat : usageStats)
        {
            LOG.info("TechnologyUsageStatisticsModel: " + stat.toPrettyString());
            // Only take the root apps.
            if (!stat.getProjectModel().equals(stat.getProjectModel().getRootProjectModel()))
                continue;
            tagsInProject.put(stat.getProjectModel(), stat.getOccurrenceCount());
        }
        return tagsInProject;
    }







    /**
     * Debug purposes.
     */
    private static void listAllTechUsageStats(GraphContext grCtx) {
        Iterable<TechnologyUsageStatisticsModel> usageStats = grCtx.findAll(TechnologyUsageStatisticsModel.class);
        for (TechnologyUsageStatisticsModel stat : usageStats)
        {
            LOG.info("STAT: " + stat.toString());
            // Only take the root apps.
            if (!stat.getProjectModel().equals(stat.getProjectModel().getRootProjectModel()))
                continue;
        }
    }

    private static void listAllApplicationModels(GraphContext grCtx)
    {
        for (ProjectModel app : getAllApplications(grCtx))
            LOG.info("App from getAllApplications(): " + app);

        final Iterable<ApplicationProjectModel> apps = grCtx.findAll(ApplicationProjectModel.class);
        for (ApplicationProjectModel appM : apps)
            LOG.info("AppProjModel: " + appM);
    }
}
