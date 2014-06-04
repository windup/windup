package org.jboss.windup.addon.reporting;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.reporting.meta.ApplicationReportModel;
import org.jboss.windup.addon.reporting.meta.ClassLoaderReportModel;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import freemarker.template.Template;

@RunWith(Arquillian.class)
public class ApplicationReportTest extends AbstractTestCase
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.core.reporting:reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(AbstractTestCase.class)
                    .addAsResource(new File("src/test/resources/reports"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.engine.core.reporting:reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testApplicationReportFreemarker() throws Exception
    {
        final File folder = File.createTempFile("windupGraph", "");
        ApplicationReportModel appReportModel = context.getFramed().addVertex(null, ApplicationReportModel.class);
        appReportModel.setApplicationName("My Great Application");

        ClassLoaderReportModel classLoaderReportModel = context.getFramed().addVertex(null,
                    ClassLoaderReportModel.class);
        classLoaderReportModel.setMyProperty("MyProperty");
        classLoaderReportModel.setReferencedFrom("Blah blah blah");

        appReportModel.addChildReport(classLoaderReportModel);

        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        Template template = cfg.getTemplate("/reports/templates/testapplicationreport.ftl");

        Map<String, Object> objects = new HashMap<String, Object>();

        objects.put("applicationReport", appReportModel);

        StringWriter sw = new StringWriter();
        template.process(objects, sw);
        String result = sw.toString();
        Assert.assertTrue(result.contains("Child noCast.referencedFrom: Blah blah blah"));
    }

    @Test
    public void testApplicationReport() throws Exception
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    /*
                     * If all conditions of the .when() clause were satisfied, the following conditions will be
                     * evaluated
                     */
                    .perform(
                                ApplicationReport.create()
                                            .applicationName("Tactical Analysis")
                                            .applicationVersion("12.3.4.12")
                                            .applicationCreator("OCP")
                    );
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        evaluationContext.put(ParameterValueStore.class, values);

        final File folder = File.createTempFile("windupGraph", "");
        Subset.evaluate(configuration).perform(new GraphRewrite(context), evaluationContext);
    }
}
