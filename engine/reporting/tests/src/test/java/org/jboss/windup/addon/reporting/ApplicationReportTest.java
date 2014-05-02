package org.jboss.windup.addon.reporting;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.reporting.meta.ApplicationReportModel;
import org.jboss.windup.addon.reporting.meta.ClassLoaderReportModel;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;

import freemarker.template.Template;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class ApplicationReportTest extends AbstractTestCase
{

    @Test
    public void testApplicationReportFreemarker() throws Exception {
        final File folder = File.createTempFile("windupGraph", "");
        final GraphContext context = new GraphContextImpl(folder);
        ApplicationReportModel appReportModel = context.getFramed().addVertex(null, ApplicationReportModel.class);
        appReportModel.setApplicationName("My Great Application");
        
        ClassLoaderReportModel classLoaderReportModel = context.getFramed().addVertex(null, ClassLoaderReportModel.class);
        classLoaderReportModel.setMyProperty("MyProperty");
        classLoaderReportModel.setReferencedFrom("Blah blah blah");
        
        appReportModel.addChildReport(classLoaderReportModel);
        
        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
        Template template = cfg.getTemplate("/reports/templates/testapplicationreport.ftl");

        class GraphCastMethod implements TemplateMethodModelEx {
            @Override
            public Object exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException
            {
                System.out.println("Args length: " + args.size());
                for (int i = 0; i < args.size(); i++) {
                    Object arg = DeepUnwrap.unwrap((TemplateModel)args.get(i));
                    System.out.println("Arg: " + arg + " class: " + arg.getClass().getName());
                }
                
                VertexFrame obj1 = (VertexFrame)DeepUnwrap.unwrap((TemplateModel)args.get(0));
                
                for (Class iface : obj1.getClass().getInterfaces()) {
                    System.out.println("Implements: " + iface);
                }
                
                for (String key : obj1.asVertex().getPropertyKeys()) {
                    System.out.println("Vertex Obj Key: " + key);
                }
                String destClass = (String)DeepUnwrap.unwrap((TemplateModel)args.get(1));
                
                try {
                    Object newObj = context.getFramed().frame(((VertexFrame)obj1).asVertex(), Class.forName(destClass));
                    return newObj;
                } catch (Exception e) {
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
        System.out.println("Results:\n"+sw);
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
        final GraphContext context = new GraphContextImpl(folder);
        Subset.evaluate(configuration).perform(new GraphRewrite(context), evaluationContext);
    }
}
