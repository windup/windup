package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationContext;
import org.jboss.windup.engine.visitor.reporter.html.model.ReportContext;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport.SourceLineAnnotationHint;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport.SourceLineAnnotations;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.SourceReportDao;
import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class JavaSourceRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(JavaSourceRenderer.class);

    @Inject
    private NamingUtility namingUtility;

    @Inject
    private JavaClassDao javaClassDao;

    @Inject
    private WindupContext context;

    @Inject
    private FileResourceDao fileResourceDao;

    @Inject
    private SourceReportDao sourceReportDao;

    private final Configuration cfg;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }
    
    public JavaSourceRenderer()
    {
        cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
    }

    @Override
    public void run()
    {
        try
        {
            for (JavaClass clz : javaClassDao.findClassesWithSource())
            {
                visitJavaClass(clz);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    @Override
    public void visitJavaClass(JavaClass entry)
    {
        try
        {

            Template template = cfg.getTemplate("/reports/templates/source.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();

            SourceReport report = new SourceReport();
            report.setSourceName(entry.getQualifiedName());
            File file = entry.getSource().asFile();
            report.setSourceBody(FileUtils.readFileToString(file));

            SourceLineAnnotations annotation = new SourceLineAnnotations(1, "Testing", "info");
            annotation.getHints().add(new SourceLineAnnotationHint("Example"));
            report.getSourceLineAnnotations().add(annotation);

            // create block settings.
            report.setSourceType("java");

            report.setSourceBlock(createBlockSettings(report.getSourceLineAnnotations()));
            objects.put("source", report);

            // create report context.
            File runDirectory = context.getRunDirectory();

            File archiveReportDirectory = new File(runDirectory, "applications");
            File archiveDirectory = new File(archiveReportDirectory, "application");
            FileUtils.forceMkdir(archiveDirectory);

            ApplicationContext context = new ApplicationContext(namingUtility.getApplicationName());
            objects.put("application", context);

            File clzDirectory = new File(archiveDirectory, "classes");
            FileUtils.forceMkdir(clzDirectory);
            ReportContext reportContext = new ReportContext(runDirectory, clzDirectory);
            objects.put("report", reportContext);

            File reportRef = new File(clzDirectory, entry.getQualifiedName() + ".html");
            template.process(objects, new FileWriter(reportRef));

            LOG.info("Wrote overview report: " + reportRef.getAbsolutePath());
            persistReportReference(entry, reportRef);
        }
        catch (Exception e)
        {
            LOG.error("Exception writing Report: " + e.getMessage());
        }
    }

    private void persistReportReference(JavaClass javaClass, File reportLocation)
    {
        // persist the file resource & reference to Java Class.
        FileResource fileReference = fileResourceDao.create();
        fileReference.setFilePath(reportLocation.getAbsolutePath());

        org.jboss.windup.graph.model.meta.report.SourceReport sourceReport = sourceReportDao.create();
        sourceReport.setReportFile(fileReference);
        sourceReport.setResource(javaClass);
        LOG.info("Added source for clz: " + javaClass.getQualifiedName());

        sourceReportDao.commit();
    }

    public String createBlockSettings(Set<SourceLineAnnotations> lines)
    {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (SourceLineAnnotations line : lines)
        {
            if (!first)
            {
                builder.append(",");
            }
            builder.append(line.getLineNumber());

            if (first)
            {
                first = false;
            }
        }

        return builder.toString();
    }
}
