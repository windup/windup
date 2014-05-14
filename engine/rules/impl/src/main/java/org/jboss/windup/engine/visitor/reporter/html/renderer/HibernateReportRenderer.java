package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationContext;
import org.jboss.windup.engine.visitor.reporter.html.model.HibernateReport;
import org.jboss.windup.engine.visitor.reporter.html.model.HibernateReport.HibernateEntityRow;
import org.jboss.windup.engine.visitor.reporter.html.model.HibernateReport.SessionPropertyRow;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class HibernateReportRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(HibernateReportRenderer.class);

    @Inject
    private WindupContext context;

    @Inject
    private NamingUtility namingUtility;

    @Inject
    private JarArchiveDao jarDao;

    private final Configuration cfg;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }
    
    public HibernateReportRenderer()
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
            Template template = cfg.getTemplate("/reports/templates/hibernate.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();
            objects.put("hibernate", generageReports());

            ApplicationContext ctx = new ApplicationContext(namingUtility.getApplicationName());
            objects.put("application", ctx);

            File runDirectory = context.getRunDirectory();
            File archiveReportDirectory = new File(runDirectory, "applications");
            File archiveDirectory = new File(archiveReportDirectory, "application");
            FileUtils.forceMkdir(archiveDirectory);
            File archiveReport = new File(archiveDirectory, "hibernate.html");

            template.process(objects, new FileWriter(archiveReport));

            LOG.info("Wrote report: " + archiveReport.getAbsolutePath());

        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    protected HibernateReport generageReports()
    {
        HibernateReport report = new HibernateReport();

        for (int i = 0; i < 10; i++)
        {
            report.getHibernateEntities().add(
                        new HibernateEntityRow("com.example.hibernate.entity.EntityName" + i, "tableName"));
        }
        for (int i = 0; i < 10; i++)
        {
            report.getSessionProperties().add(new SessionPropertyRow("exampleProperty" + i, "exampleValue" + i));
        }
        return report;
    }
}
