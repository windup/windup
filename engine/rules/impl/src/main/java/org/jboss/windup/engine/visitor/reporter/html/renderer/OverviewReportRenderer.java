package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.Level;
import org.jboss.windup.engine.visitor.reporter.html.model.LinkName;
import org.jboss.windup.engine.visitor.reporter.html.model.OverviewReport;
import org.jboss.windup.engine.visitor.reporter.html.model.Tag;
import org.jboss.windup.graph.WindupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class OverviewReportRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(OverviewReportRenderer.class);

    @Inject
    private WindupContext context;

    private final Configuration cfg;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }
    
    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return generateDependencies(ApplicationReportRenderer.class, BlacklistClassloaderReportRenderer.class, CssJsResourceRenderer.class, DuplicateClassloaderReportRenderer.class, EJBReportRenderer.class, HibernateReportRenderer.class, JavaSourceRenderer.class, ManifestSourceRenderer.class, NotFoundClassloaderReportRenderer.class, PropertiesSourceRenderer.class, ServerResourceReportRenderer.class, SpringReportRenderer.class, XmlSourceRenderer.class);
    }
    
    public OverviewReportRenderer()
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
            Template template = cfg.getTemplate("/reports/templates/overview.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();
            objects.put("applications", generageReports());
            File runDirectory = context.getRunDirectory();
            File overviewReport = new File(runDirectory, "index.html");
            template.process(objects, new FileWriter(overviewReport));

            LOG.info("Wrote overview report: " + overviewReport.getAbsolutePath());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    protected List<OverviewReport> generageReports()
    {
        List<OverviewReport> overviewReports = new LinkedList<>();
        OverviewReport report = new OverviewReport();
        overviewReports.add(report);
        LinkName applicationLink = new LinkName("#", "application.war");
        report.setApplicationLink(applicationLink);
        report.getTechnologyTags().add(new Tag("WAR", Level.INFO));
        report.getIssueTags().add(new Tag("Testing", Level.DANGER));
        return overviewReports;
    }
}
