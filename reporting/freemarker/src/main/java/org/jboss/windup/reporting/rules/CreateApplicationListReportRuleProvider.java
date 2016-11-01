package org.jboss.windup.reporting.rules;

import java.util.*;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
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
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders an application index page listing all applications analyzed by the current execution of windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
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
        WindupVertexListModel<ApplicationReportModel> appsListVertex = listService.create();
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
        relatedData.put("applications", appsListVertex);

        // Our current model doesn't keep the list order, but once I wrote, I'm leaving the sorting here for when it does.
        Collections.sort(appsList, new AppRootFileNameComparator());

        for (ApplicationReportModel applicationReportModel : appsList)
            appsListVertex.addItem(applicationReportModel);

        report.setRelatedResource(relatedData);
    }



    private static class AppRootFileNameComparator implements Comparator<ApplicationReportModel>
    {
        public int compare(ApplicationReportModel o1, ApplicationReportModel o2)
        {
            // If the info is missing, put that to the end. This may be the case of virtual apps.
            if (null == o1.getProjectModel() || null == o1.getProjectModel().getRootFileModel() || null == o1.getProjectModel().getRootFileModel().getFileName() )
                return 1;
            if (null == o2.getProjectModel() || null == o2.getProjectModel().getRootFileModel() || null == o2.getProjectModel().getRootFileModel().getFileName() )
                return -1;

            // On error resume next.
            try {
                return ObjectUtils.compare(
                    o1.getProjectModel().getRootFileModel().getFileName(),
                    o2.getProjectModel().getRootFileModel().getFileName());
            }
            catch (Throwable ex)
            {
                return 0;
            }
        }
    }
}
