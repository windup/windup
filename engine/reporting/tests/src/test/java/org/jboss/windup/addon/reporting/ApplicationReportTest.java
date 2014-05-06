package org.jboss.windup.addon.reporting;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.reporting.meta.ApplicationReportModel;
import org.jboss.windup.addon.reporting.meta.ClassLoaderReportModel;
import org.jboss.windup.graph.GraphContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import com.tinkerpop.frames.VertexFrame;

import freemarker.template.Template;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

@RunWith(Arquillian.class)
public class ApplicationReportTest extends AbstractTestCase
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.addon.reporting:reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(AbstractTestCase.class)
                    .addAsResource(new File("src/test/resources/reports"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.addon.reporting:reporting"),
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

        class GraphCastMethod implements TemplateMethodModelEx
        {
            @Override
            public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException
            {
                System.out.println("Args length: " + args.size());
                for (int i = 0; i < args.size(); i++)
                {
                    Object arg = DeepUnwrap.unwrap((TemplateModel) args.get(i));
                    System.out.println("Arg: " + arg + " class: " + arg.getClass().getName());
                }

                VertexFrame obj1 = (VertexFrame) DeepUnwrap.unwrap((TemplateModel) args.get(0));

                for (Class iface : obj1.getClass().getInterfaces())
                {
                    System.out.println("Implements: " + iface);
                }

                for (String key : obj1.asVertex().getPropertyKeys())
                {
                    System.out.println("Vertex Obj Key: " + key);
                }
                String destClass = (String) DeepUnwrap.unwrap((TemplateModel) args.get(1));

                try
                {
                    Object newObj = context.getFramed()
                                .frame(((VertexFrame) obj1).asVertex(), Class.forName(destClass));
                    return newObj;
                }
                catch (Exception e)
                {
                    throw new TemplateModelException(e);
                }
            }
        }

        Map<String, Object> objects = new HashMap<String, Object>();

        GraphCastMethod graphCast = new GraphCastMethod();
        objects.put("applicationReport", appReportModel);
        objects.put("graphCast", graphCast);

        StringWriter sw = new StringWriter();
        template.process(objects, sw);
        System.out.println("Results:\n" + sw);
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
