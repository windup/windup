package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationReport;
import org.jboss.windup.engine.visitor.reporter.html.model.ArchiveReport;
import org.jboss.windup.engine.visitor.reporter.html.model.ArchiveReport.ResourceReportRow;
import org.jboss.windup.engine.visitor.reporter.html.model.Level;
import org.jboss.windup.engine.visitor.reporter.html.model.SimpleName;
import org.jboss.windup.engine.visitor.reporter.html.model.Tag;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.EJBConfigurationDao;
import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.dao.PropertiesDao;
import org.jboss.windup.graph.dao.WebConfigurationDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.ApplicationReferenceModel;
import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ApplicationReportRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReportRenderer.class);

    @Inject
    private WindupContext context;

    @Inject
    private ApplicationReferenceDao appRefDao;

    @Inject
    private JavaClassDao javaDao;

    @Inject
    private XmlResourceDao xmlDao;

    @Inject
    private JarManifestDao manifestDao;

    @Inject
    private ArchiveDao archiveDao;

    @Inject
    private NamingUtility namingUtility;

    @Inject
    private WebConfigurationDao webConfigurationDao;

    @Inject
    private EJBConfigurationDao ejbConfigurationDao;

    @Inject
    private MavenFacetDao mavenDao;

    @Inject
    private PropertiesDao propertiesDao;

    private Configuration cfg;
    private File runDirectory;
    private File reportReference;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }

    @PostConstruct
    private void postConstruct() throws IOException
    {
        this.cfg = new Configuration();
        this.cfg.setTemplateUpdateDelay(500);
        this.cfg.setClassForTemplateLoading(this.getClass(), "/");

        this.runDirectory = context.getRunDirectory();
        File archiveReportDirectory = new File(this.runDirectory, "applications");
        File archiveDirectory = new File(archiveReportDirectory, "application");
        FileUtils.forceMkdir(archiveDirectory);
        reportReference = new File(archiveDirectory, "index.html");
    }

    @Override
    public void run()
    {
        try
        {
            ApplicationReport applicationReport = new ApplicationReport();
            for (ApplicationReferenceModel app : appRefDao.getAll())
            {
                ArchiveResourceModel reference = app.getArchive();
                applicationReport.setApplicationName(reference.getArchiveName());
                recurseArchive(applicationReport, app.getArchive());
            }

            Template template = this.cfg.getTemplate("/reports/templates/application.ftl");

            java.util.Map<String, Object> objects = new HashMap<String, Object>();
            objects.put("application", applicationReport);

            template.process(objects, new FileWriter(reportReference));
            LOG.info("Wrote report: " + reportReference.getAbsolutePath());

        }
        catch (Exception ex)
        {
            throw new RuntimeException("Exception writing report:\n    " + ex.getMessage(), ex);
        }
    }

    protected void recurseArchive(ApplicationReport report, ArchiveResourceModel resource)
    {
        ArchiveReport archiveReport = new ArchiveReport();

        String name = null;
        if (resource.getParentResource() instanceof ArchiveEntryResourceModel)
        {
            ArchiveEntryResourceModel parentEntry = (ArchiveEntryResourceModel) resource.getParentResource();
            name = namingUtility.buildFullPath(parentEntry);
        }
        else
        {
            name = resource.getArchiveName();
        }

        archiveReport.setApplicationPath(name);

        for (ArchiveEntryResourceModel entry : resource.getChildrenArchiveEntries())
        {
            // check to see about facets.
            processEntry(archiveReport, entry);
        }

        for (ArchiveResourceModel childResource : resource.getChildrenArchive())
        {
            recurseArchive(report, childResource);
        }

        report.getArchives().add(archiveReport);
    }

    protected void processEntry(ArchiveReport report, ArchiveEntryResourceModel entry)
    {
        ResourceReportRow reportRow = new ResourceReportRow();

        // see if the resource is a java class...
        if (javaDao.isJavaClass(entry))
        {
            JavaClassModel clz = javaDao.getJavaClassFromResource(entry);
            if (!clz.isCustomerPackage())
            {
                return;
            }
            reportRow.setResourceName(namingUtility.getReportJavaResource(this.runDirectory, reportReference, clz));
            reportRow.getTechnologyTags().add(new Tag("Java", Level.SUCCESS));

            report.getResources().add(reportRow);
            return;
        }

        // check if it is a XML resource...
        if (xmlDao.isXmlResource(entry))
        {
            XmlResourceModel resource = xmlDao.getXmlFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportXmlResource(this.runDirectory, reportReference, resource));
            reportRow.getTechnologyTags().add(new Tag("XML", Level.SUCCESS));

            // check the XML for some tags...
            if (ejbConfigurationDao.isEJBConfiguration(resource))
            {
                EjbConfigurationFacetModel ejbConfiguration = ejbConfigurationDao
                            .getEjbConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(
                            new Tag("EJB " + ejbConfiguration.getSpecificationVersion() + " Configuration",
                                        Level.SUCCESS));
            }

            if (webConfigurationDao.isWebConfiguration(resource))
            {
                WebConfigurationFacetModel webConfiguration = webConfigurationDao
                            .getWebConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(
                            new Tag("Web " + webConfiguration.getSpecificationVersion() + " Configuration",
                                        Level.SUCCESS));
            }

            if (mavenDao.isMavenConfiguration(resource))
            {
                MavenFacetModel mavenConfiguration = mavenDao.getMavenConfigurationFromResource(resource);
                reportRow.getTechnologyTags().add(
                            new Tag("Maven " + mavenConfiguration.getSpecificationVersion() + " Configuration",
                                        Level.SUCCESS));
            }

            report.getResources().add(reportRow);
            return;
        }

        if (propertiesDao.isPropertiesResource(entry))
        {
            PropertiesMetaModel meta = propertiesDao.getPropertiesFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportPropertiesResource(this.runDirectory, reportReference,
                        meta));
            reportRow.getTechnologyTags().add(new Tag("Properties", Level.SUCCESS));
            report.getResources().add(reportRow);
            return;
        }

        // check if it is a manifest...
        if (manifestDao.isManifestResource(entry))
        {
            JarManifestModel resource = manifestDao.getManifestFromResource(entry);
            reportRow.setResourceName(namingUtility.getReportManifestResource(this.runDirectory, reportReference,
                        resource));
            reportRow.getTechnologyTags().add(new Tag("Manifest", Level.SUCCESS));

            report.getResources().add(reportRow);
            return;
        }

        if (archiveDao.isArchiveResource(entry))
        {
            // skip.
            return;
        }

        reportRow.setResourceName(new SimpleName(entry.getArchiveEntry()));
        reportRow.getIssueTags().add(new Tag("Unknown Type", Level.WARNING));

        report.getResources().add(reportRow);
        return;

    }

}
