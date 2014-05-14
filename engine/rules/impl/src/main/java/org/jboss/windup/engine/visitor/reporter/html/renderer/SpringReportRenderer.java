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
import org.jboss.windup.engine.visitor.reporter.html.model.SpringReport;
import org.jboss.windup.engine.visitor.reporter.html.model.SpringReport.SpringBeanJNDIRow;
import org.jboss.windup.engine.visitor.reporter.html.model.SpringReport.SpringBeanResourceRow;
import org.jboss.windup.engine.visitor.reporter.html.model.SpringReport.SpringBeanRow;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class SpringReportRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(SpringReportRenderer.class);

    @Inject
    private WindupContext context;

    @Inject
    private JarArchiveDao jarDao;

    @Inject
    private NamingUtility namingUtility;
    private final Configuration cfg;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }
    
    public SpringReportRenderer()
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
            Template template = cfg.getTemplate("/reports/templates/spring.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();

            ApplicationContext appCtx = new ApplicationContext(namingUtility.getApplicationName());
            objects.put("application", appCtx);

            objects.put("spring", generageReports());

            File runDirectory = context.getRunDirectory();
            File archiveReportDirectory = new File(runDirectory, "applications");
            File archiveDirectory = new File(archiveReportDirectory, "application");
            FileUtils.forceMkdir(archiveDirectory);
            File archiveReport = new File(archiveDirectory, "spring.html");

            template.process(objects, new FileWriter(archiveReport));

            LOG.info("Wrote report: " + archiveReport.getAbsolutePath());

        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    protected SpringReport generageReports()
    {
        SpringReport report = new SpringReport();

        for (int i = 0; i < 10; i++)
        {
            report.getSpringBeans().add(new SpringBeanRow("exampleBean" + i, "com.example.spring.SpringBeanName" + i));
        }
        for (int i = 0; i < 10; i++)
        {
            report.getSpringJNDIBeans().add(
                        new SpringBeanJNDIRow("exampleBean" + i, "com.example.spring.SpringBeanName" + i,
                                    "java:/exampleBeanName" + i));
        }
        for (int i = 0; i < 10; i++)
        {
            report.getSpringResourceBeans().add(
                        new SpringBeanResourceRow("exampleBean" + i, "com.example.spring.SpringBeanName" + i,
                                    "Database"));
        }
        return report;
    }
}
