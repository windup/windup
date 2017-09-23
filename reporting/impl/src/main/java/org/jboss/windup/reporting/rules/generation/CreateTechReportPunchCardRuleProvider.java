package org.jboss.windup.reporting.rules.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
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

    private static final String TECH_HIERARCHY_TAGS_FILE = "techReport-techHierarchy-punchCard.tags.xml";


    @Inject private TagServiceHolder tagServiceHolder;



    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            /// TODO: Move this to a special rule provider.
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    new TagGraphService(event.getGraphContext()).feedTheWholeTagStructureToGraph(tagServiceHolder.getTagService());
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


            // TODO: Maybe it would be better to query like this?
            // For each application,
            for (FileModel inputPath : WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths())
            {
                LOG.info((String)inputPath.asVertex().getProperty(WindupVertexFrame.TYPE_PROP));
            }
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

        // What sectors (column groups) and tech-groups (columns) should be on the report. View, Connect, Store, Sustain, ...
        Tag sectorsTag = tagServiceHolder.getTagService().getTag(TechReportPunchCardModel.TAG_NAME_SECTORS);
        if (null == sectorsTag)
            throw new WindupException("Tech report hierarchy definition tag, '"+TechReportPunchCardModel.TAG_NAME_SECTORS+"', not found.");

        for (Tag tag1 : sectorsTag.getContainedTags())
        {
            for (Tag tag2 : tag1.getContainedTags())
            {
                String tagName = tag2.getName();
                Map<ProjectModel, Integer> tagCountForAllApps = getTagCountForAllApps(grCtx, tagName);
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
     * @return Map of counts of given tag occurrences in all root applications.
     *
     * TODO This is inconvenient as the template needs things grouped by app, not by technology...
     */
    public static Map<ProjectModel, Integer> getTagCountForAllApps(GraphContext grCtx, String tagName) {
        final GraphService<TechnologyUsageStatisticsModel> techUsageService = new GraphService<>(grCtx, TechnologyUsageStatisticsModel.class);
        Iterable<TechnologyUsageStatisticsModel> usageStats = techUsageService.findAllByProperty(TechnologyUsageStatisticsModel.NAME, tagName);

        Map<ProjectModel, Integer> tagsInProject = new HashMap();
        usageStats.forEach(stat -> {
            // Only take root apps.
            if (!stat.getProjectModel().equals(stat.getProjectModel().getRootProjectModel()))
                return;
            tagsInProject.put(stat.getProjectModel(), stat.getOccurrenceCount());
        });
        return tagsInProject;
    }
}
