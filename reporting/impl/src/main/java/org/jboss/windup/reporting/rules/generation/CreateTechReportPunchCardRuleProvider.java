package org.jboss.windup.reporting.rules.generation;

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
import org.jboss.windup.config.tags.TagService;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationInputPathModel;
import org.jboss.windup.graph.model.ApplicationProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.*;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.TagGraphService;
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


    public static final String TEMPLATE_PATH = "/reports/templates/techReport-punchCard.ftl";
    public static final String REPORT_DESCRIPTION =
            "This report is a statistic of technologies occurences in the input applications."
            + " It shows how the technologies are distributed and is mostly useful when analysing many applications.";

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
        private static final String REPORT_NAME = "Technologies";

        @Override
        public void perform(GraphRewrite event, EvaluationContext evCtx)
        {
            GraphContext grCtx = event.getGraphContext();

            listAllTechUsageStats(grCtx);
            listAllApplicationModels(grCtx);

            // Create the report model.
            TechReportPunchCardModel report = createGlobalReport(grCtx);

            // Add sectors to it.
            GraphService<TagModel> service = new GraphService<>(grCtx, TagModel.class);
            //TagModel sectorsTag = service.getUniqueByProperty(TagModel.PROP_NAME, TechReportPunchCardModel.TAG_NAME_SECTORS.toLowerCase());
            TagModel sectorsTag = new TagGraphService(event.getGraphContext()).getTagByName(TechReportPunchCardModel.TAG_NAME_SECTORS);

            if (null == sectorsTag)
                throw new WindupException("Tech sectors tag, '" + TechReportPunchCardModel.TAG_NAME_SECTORS
                        + "', not found. It defines the structure of the punchcard report.");
            report.setSectorsHolderTag(sectorsTag);

            // Now let's fill it with data.
            Map<ProjectModel, Map<String, Integer>> countsOfTagsInApps = computeProjectAndTagsMatrix(grCtx);

            // Find maximum number of occurences within the apps. Used for cirle size.
            Map<String, Integer> maximumsPerTech = computeMaxCountPerTag(countsOfTagsInApps);
            report.setMaximumCounts(maximumsPerTech);


            // TODO: Maybe it would be better to query like this?
            // For each application,
            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths())
            {
                List types = (List)inputPath.asVertex().getProperty(WindupVertexFrame.TYPE_PROP);
                LOG.info("InputPath type:" + types.toString());
            }
        }

        private Map<String,Integer> computeMaxCountPerTag(Map<ProjectModel, Map<String, Integer>> countsOfTagsInApps)
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

        private TechReportPunchCardModel createGlobalReport(GraphContext context)
        {
            ReportService reportService = new ReportService(context);
            ApplicationReportService applicationReportService = new ApplicationReportService(context);
            ApplicationReportModel report = applicationReportService.create();
            report.setReportName(REPORT_NAME);
            report.setTemplatePath(TEMPLATE_PATH);
            report.setTemplateType(TemplateType.FREEMARKER);
            reportService.setUniqueFilename(report, "techReport-punchCard", "html");
            report.setDisplayInApplicationReportIndex(true);
            report.setDisplayInGlobalApplicationIndex(true);
            report.setReportPriority(101);
            report.setReportIconClass("glyphicon glyphicon-tags");
            report.setDescription(REPORT_DESCRIPTION);

            TechReportPunchCardModel punchcard = new GraphService<>(context, TechReportPunchCardModel.class).addTypeToModel(report);
            return punchcard;
        }


    }

    /*
        Needs TagService, which I don't know how to get from a Freemarker method.
     */
    private Map<ProjectModel, Map<String, Integer>> computeProjectAndTagsMatrix(GraphContext grCtx) {
        // App -> tag name -> occurences.
        Map<ProjectModel, Map<String, Integer>> countsOfTagsInApps = new HashMap<>();
        Map<String, Integer> maxCountPerTag = new HashMap<>();

        // What sectors (column groups) and sub-sectors (columns) should be on the report. View, Connect, Store, Sustain, ...
        Tag sectorsTag = tagServiceHolder.getTagService().getTag(TechReportPunchCardModel.TAG_NAME_SECTORS);
        if (null == sectorsTag)
            throw new WindupException("Tech report hierarchy definition tag, '"+TechReportPunchCardModel.TAG_NAME_SECTORS+"', not found.");

        // For each sector / subsector
        for (Tag tag1 : sectorsTag.getContainedTags())
        {
            for (Tag tag2 : tag1.getContainedTags())
            {
                String tagName = tag2.getName();
                Map<ProjectModel, Integer> tagCountForAllApps = getTagCountForAllApps(grCtx, tagName);
                LOG.info("Computed tag " + tagName + ":\n" + printMap(tagCountForAllApps, true));///

                // Transpose the results from getTagCountForAllApps, so that 1st level keys are the apps.
                tagCountForAllApps.forEach((project, count) -> {
                    Map<String, Integer> appTagCounts = countsOfTagsInApps.computeIfAbsent(project, k -> new HashMap<>());
                    appTagCounts.put(tagName, count);
                });
            }
        }

        return countsOfTagsInApps;
    }

    /**
     * Formats a Map to a String, each entry as one line, using toString() of keys and values.
     */
    private static String printMap(Map<ProjectModel, Integer> tagCountForAllApps, boolean valueFirst)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ProjectModel, Integer> e : tagCountForAllApps.entrySet())
        {
            sb.append("  ");
            sb.append(valueFirst ? e.getValue() : e.getKey());
            sb.append(": ");
            sb.append(valueFirst ? e.getKey() : e.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @return Map of counts of given tag and subtags occurrences in all input applications.
     *         I.e. how many items tagged with any tag under subSectorTag are there in each input application.
     */
    public static Map<ProjectModel, Integer> getTagCountForAllApps(GraphContext grCtx, String subSectorTagName)
    {
        // Get all "subtags" of this tag.
        //Set<String> subTagsNames = getSubTagNames_tagService(subSectorTagName);
        Set<String> subTagsNames = getSubTagNames_graph(grCtx, subSectorTagName);

        // Get all apps.
        Set<ApplicationProjectModel> apps = getAllApplications(grCtx);

        Map<ProjectModel, Integer> appToTechSectorCoveredTagsOccurrenceCount = new HashMap<>();

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
            appToTechSectorCoveredTagsOccurrenceCount.put(app, countSoFar);
        }
        return appToTechSectorCoveredTagsOccurrenceCount;
    }

    private static Set<String> getSubTagNames_graph(GraphContext grCtx, String subSectorTagName)
    {
        TagGraphService tagService = new TagGraphService(grCtx);
        Set<TagModel> subTags = tagService.getDescendantTags(tagService.getTagByName(subSectorTagName));
        return subTags.stream().map(t->t.getName()).collect(Collectors.toSet());
    }

    private Set<String> getSubTagNames_tagService(String subSectorTagName)
    {
        TagService tagService = this.tagServiceHolder.getTagService();
        Set<Tag> subTags = tagService.getDescendantTags(tagService.getTag(subSectorTagName));
        return subTags.stream().map(t->t.getName()).collect(Collectors.toSet());
    }

    /**
     * Trying to figure out here which projects are apps.
     */
    private static Set<ProjectModel> getAllApplications_(GraphContext grCtx)
    {
        new ProjectService(grCtx).getRootProjectModels();

        Set<ProjectModel> apps = new HashSet<>();
        Iterable<ProjectModel> projects = grCtx.findAll(ProjectModel.class);
        for (ProjectModel proj : projects)
        {
            for (ProjectModel app: proj.getApplications() )
                apps.add(app.getRootProjectModel());
        }
        return apps;
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
    public static Map<ProjectModel, Integer> getTagCountForAllApps_nonDeep(GraphContext grCtx, String subSectorTagName) {
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
    public static void listAllTechUsageStats(GraphContext grCtx) {
        Iterable<TechnologyUsageStatisticsModel> usageStats = grCtx.findAll(TechnologyUsageStatisticsModel.class);
        for (TechnologyUsageStatisticsModel stat : usageStats)
        {
            LOG.info("STAT: " + stat.toString());
            // Only take the root apps.
            if (!stat.getProjectModel().equals(stat.getProjectModel().getRootProjectModel()))
                continue;
        }
    }

    private void listAllApplicationModels(GraphContext grCtx)
    {
        for (ProjectModel app : getAllApplications(grCtx))
            LOG.info("App from getAllApplications(): " + app);

        final Iterable<ApplicationProjectModel> apps = grCtx.findAll(ApplicationProjectModel.class);
        for (ApplicationProjectModel appM : apps)
            LOG.info("AppProjModel: " + appM);
    }
}
