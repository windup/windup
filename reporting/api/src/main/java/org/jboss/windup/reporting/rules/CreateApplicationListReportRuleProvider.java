package org.jboss.windup.reporting.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders an application index page listing all applications analyzed by the current execution of windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class CreateApplicationListReportRuleProvider extends AbstractRuleProvider
{
    public static final String APPLICATION_LIST_REPORT = "Application List";
    private static final String OUTPUT_FILENAME = "../index.html";
    public static final String TEMPLATE_PATH = "/reports/templates/application_list.ftl";

    @Inject
    private Furnace furnace;


    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    createIndexReport(event.getGraphContext());
                }
            });
    }
    // @formatter:on

    private void createIndexReport(GraphContext context)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(context);

        ApplicationReportModel report = applicationReportService.create();
        report.setReportPriority(1);
        report.setReportIconClass("glyphicon glyphicon-home");
        report.setReportName(APPLICATION_LIST_REPORT);
        report.setTemplatePath(TEMPLATE_PATH);
        report.setTemplateType(TemplateType.FREEMARKER);

        report.setDisplayInApplicationReportIndex(false);
        report.setReportFilename(OUTPUT_FILENAME);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> relatedData = new HashMap<>();
        final Iterable<ApplicationReportModel> apps = applicationReportService.findAll();
        List<ApplicationReportModel> appsList = new ArrayList();
        for (ApplicationReportModel applicationReportModel : apps)
        {
            if (applicationReportModel.isMainApplicationReport() != null && applicationReportModel.isMainApplicationReport())
            {
                appsList.add(applicationReportModel);
                if (ProjectService.SHARED_LIBS_UNIQUE_ID.equals(applicationReportModel.getProjectModel().getUniqueID()))
                    relatedData.put("sharedLibsApplicationReport", applicationReportModel); // Used as kind of boolean in the template.
            }
        }

        // Our current model doesn't keep the list order, but once I wrote, I'm leaving the sorting here for when it does.
        Collections.sort(appsList, new AppRootFileNameComparator());
        Logger.getLogger(CreateApplicationListReportRuleProvider.class).info("AppList sorted:\n    " + StringUtils.join(appsList, "\n    "));///
        WindupVertexListModel<ApplicationReportModel> appsListVertex = listService.create();
        relatedData.put("applications", appsListVertex);
        for (ApplicationReportModel applicationReportModel : appsList)
            appsListVertex.addItem(applicationReportModel);

        /// Test whether it remains sorted when queried.
        List<ApplicationReportModel> appsSorted = new ArrayList<>();
        appsListVertex.getList().iterator().forEachRemaining(appsSorted::add);
        String names = appsSorted.stream().map(arm -> arm.getProjectModel().getRootFileModel().getFileName()).collect(Collectors.joining("\n    "));
        Logger.getLogger(CreateApplicationListReportRuleProvider.class).info("AppList queried:\n    " + String.join("\n    ",  names));

        report.setRelatedResource(relatedData);
    }



    private static class AppRootFileNameComparator implements Comparator<ApplicationReportModel>
    {
        public int compare(ApplicationReportModel o1, ApplicationReportModel o2)
        {
            Logger.getLogger(CreateApplicationListReportRuleProvider.class).info(
                    "Comparing " + o1.getProjectModel().getRootFileModel().getFileName() +
                    " and      " + o2.getProjectModel().getRootFileModel().getFileName());///

            // If the info is missing, put that to the end. This may be the case of virtual apps.
            if (null == o1.getProjectModel() || null == o1.getProjectModel().getRootFileModel() || null == o1.getProjectModel().getRootFileModel().getFileName() )
                return 1;
            if (null == o2.getProjectModel() || null == o2.getProjectModel().getRootFileModel() || null == o2.getProjectModel().getRootFileModel().getFileName() )
                return -1;

            try {
                return o1.getProjectModel().getRootFileModel().getFileName().compareToIgnoreCase(o2.getProjectModel().getRootFileModel().getFileName());
                //return Comparator.comparing((ApplicationReportModel o) -> o.getProjectModel().getRootFileModel().getFileName(), String::compareToIgnoreCase).compare(o1, o2);
            }
            catch (Throwable ex)
            {
                return 0;
            }
        }
    }

    ///
    public static void main(String[] args)
    {
        List<String> list = Arrays.asList(new String[]{"C", "b", "ab", "A", "D"});
        //Collections.sort(list, Comparator.comparing((String str) -> str, String::compareToIgnoreCase));
        Collections.sort(list, Comparator.comparing((String o) -> o, String::compareToIgnoreCase));
        System.out.println("AppList sorted:\n    " + String.join("\n    ", list));
    }
}
