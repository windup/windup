package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationContext;
import org.jboss.windup.engine.visitor.reporter.html.model.EJBReport;
import org.jboss.windup.engine.visitor.reporter.html.model.EJBReport.EJBRow;
import org.jboss.windup.engine.visitor.reporter.html.model.EJBReport.MDBRow;
import org.jboss.windup.engine.visitor.reporter.html.model.Name;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.EJBEntityDao;
import org.jboss.windup.graph.dao.EJBSessionBeanDao;
import org.jboss.windup.graph.dao.MessageDrivenDao;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class EJBReportRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(EJBReportRenderer.class);

    @Inject
    private NamingUtility namingUtility;

    @Inject
    private WindupContext context;

    @Inject
    private EJBSessionBeanDao sessionDao;

    @Inject
    private EJBEntityDao entityDao;

    @Inject
    private SourceReport sourceReportDao;

    @Inject
    private MessageDrivenDao messageDrivenDao;

    @Inject
    private NamingUtility reportUtility;

    private Configuration cfg;
    private File runDirectory;
    private File reportReference;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }
    
    @PostConstruct
    private void postConstruct() throws IOException
    {
        cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");

        runDirectory = context.getRunDirectory();
        File archiveReportDirectory = new File(runDirectory, "applications");
        File archiveDirectory = new File(archiveReportDirectory, "application");
        FileUtils.forceMkdir(archiveDirectory);
        reportReference = new File(archiveDirectory, "ejbs.html");
    }

    @Override
    public void run()
    {
        try
        {
            Template template = cfg.getTemplate("/reports/templates/ejb.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();

            ApplicationContext context = new ApplicationContext(namingUtility.getApplicationName());
            objects.put("application", context);

            objects.put("ejbs", generageReports());
            template.process(objects, new FileWriter(reportReference));
            LOG.info("Wrote report: " + reportReference.getAbsolutePath());

        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    protected EJBRow getEJBRow(File runDirectory, File reportReference, String title, JavaClass clz, String type)
    {
        if (clz == null)
        {
            return null;
        }
        Name name = reportUtility.getReportJavaResource(runDirectory, reportReference, clz);
        EJBRow ejbRow = new EJBRow(title, name, type);
        return ejbRow;
    }

    protected EJBReport generageReports()
    {
        EJBReport applicationReport = new EJBReport();

        for (EjbSessionBeanFacet session : sessionDao.getAll())
        {
            EJBRow ejbRow = getEJBRow(runDirectory, reportReference, session.getSessionBeanName(),
                        session.getJavaClassFacet(), session.getSessionType());
            EJBRow ejbHome = getEJBRow(runDirectory, reportReference, session.getSessionBeanName(),
                        session.getEjbHome(), "Home");
            EJBRow ejbRemote = getEJBRow(runDirectory, reportReference, session.getSessionBeanName(),
                        session.getEjbRemote(), "Remote");
            EJBRow ejbLocalHome = getEJBRow(runDirectory, reportReference, session.getSessionBeanName(),
                        session.getEjbLocalHome(), "LocalHome");
            EJBRow ejbLocal = getEJBRow(runDirectory, reportReference, session.getSessionBeanName(),
                        session.getEjbLocal(), "Local");

            String type = session.getSessionType();

            if (StringUtils.equals("Stateless", type))
            {
                applicationReport.getStatelessBeans().add(ejbRow);

                if (ejbHome != null)
                    applicationReport.getStatelessBeans().add(ejbHome);
                if (ejbRemote != null)
                    applicationReport.getStatelessBeans().add(ejbRemote);
                if (ejbLocalHome != null)
                    applicationReport.getStatelessBeans().add(ejbLocalHome);
                if (ejbLocal != null)
                    applicationReport.getStatelessBeans().add(ejbLocal);
            }
            else
            {
                applicationReport.getStatefulBeans().add(ejbRow);

                if (ejbHome != null)
                    applicationReport.getStatefulBeans().add(ejbHome);
                if (ejbRemote != null)
                    applicationReport.getStatefulBeans().add(ejbRemote);
                if (ejbLocalHome != null)
                    applicationReport.getStatefulBeans().add(ejbLocalHome);
                if (ejbLocal != null)
                    applicationReport.getStatefulBeans().add(ejbLocal);
            }
        }

        for (MessageDrivenBeanFacet mdf : messageDrivenDao.getAll())
        {
            String name = mdf.getMessageDrivenBeanName();
            if (StringUtils.isBlank(name))
            {
                name = mdf.getJavaClassFacet().getQualifiedName();
            }
            Name qualifiedName = reportUtility.getReportJavaResource(runDirectory, reportReference,
                        mdf.getJavaClassFacet());

            MDBRow row = new MDBRow(name, qualifiedName, "");
            applicationReport.getMdbs().add(row);
        }
        return applicationReport;
    }

}
