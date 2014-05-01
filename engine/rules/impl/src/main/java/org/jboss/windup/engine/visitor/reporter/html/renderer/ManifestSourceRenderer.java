package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.ApplicationContext;
import org.jboss.windup.engine.visitor.reporter.html.model.ReportContext;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport;
import org.jboss.windup.engine.visitor.reporter.html.model.SourceReport.SourceLineAnnotations;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.dao.SourceReportDao;
import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class ManifestSourceRenderer extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ManifestSourceRenderer.class);

    @Inject
    private JarManifestDao manifestResources;

    @Inject
    private FileResourceDao fileResourceDao;

    @Inject
    private SourceReportDao sourceReportDao;

    @Inject
    private NamingUtility namingUtility;

    @Inject
    private WindupContext context;

    @Inject
    private GraphUtil graphUtil;
    
    private final Configuration cfg;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }
    
    public ManifestSourceRenderer()
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
            for (JarManifestModel entry : manifestResources.getAll())
            {
                visitManifest(entry);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception writing report.", e);
        }
    }

    @Override
    public void visitManifest(JarManifestModel entry)
    {

        try
        {
            Template template = cfg.getTemplate("/reports/templates/source.ftl");

            Map<String, Object> objects = new HashMap<String, Object>();

            ApplicationContext ctx = new ApplicationContext(namingUtility.getApplicationName());
            objects.put("application", ctx);

            SourceReport report = new SourceReport();

            report.setSourceBody(IOUtils.toString(entry.asInputStream()));

            // create block settings.
            report.setSourceType("manifest");

            report.setSourceBlock(createBlockSettings(report.getSourceLineAnnotations()));
            objects.put("source", report);

            // create report context.
            File runDirectory = context.getRunDirectory();

            File archiveReportDirectory = new File(runDirectory, "applications");
            File archiveDirectory = new File(archiveReportDirectory, "application");
            FileUtils.forceMkdir(archiveDirectory);

            File clzDirectory = new File(archiveDirectory, "resources");

            String fullName = null;
            String name = null;
            if (entry.getResource() instanceof ArchiveEntryResourceModel)
            {
                ArchiveEntryResourceModel resource = graphUtil.castToType(entry.getResource().asVertex(), ArchiveEntryResourceModel.class);
                name = resource.getArchiveEntry();
                name = StringUtils.substringAfterLast(name, "/");

                fullName = namingUtility.buildFullPath(resource);
            }
            else if (entry.getResource() instanceof FileResourceModel)
            {
                FileResourceModel resource = graphUtil.castToType(entry.getResource().asVertex(), FileResourceModel.class);
                name = resource.asFile().getName();

                fullName = name;
            }
            report.setSourceName(name);

            File reportRef = new File(clzDirectory, fullName + ".html");
            FileUtils.forceMkdir(reportRef.getParentFile());

            ReportContext reportContext = new ReportContext(runDirectory, reportRef.getParentFile());
            objects.put("report", reportContext);

            template.process(objects, new FileWriter(reportRef));

            LOG.info("Wrote overview report: " + reportRef.getAbsolutePath());

            persistReportReference(entry, reportRef);
        }
        catch (Exception e)
        {
            LOG.error("Exception writing Report: " + e.getMessage());
        }
    }

    private void persistReportReference(JarManifestModel xmlResource, File reportLocation)
    {
        // persist the file resource & reference to Java Class.
        FileResourceModel fileReference = fileResourceDao.create();
        fileReference.setFilePath(reportLocation.getAbsolutePath());

        org.jboss.windup.graph.model.meta.report.SourceReportModel sourceReport = sourceReportDao.create();
        sourceReport.setReportFile(fileReference);
        sourceReport.setResource(xmlResource);

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
