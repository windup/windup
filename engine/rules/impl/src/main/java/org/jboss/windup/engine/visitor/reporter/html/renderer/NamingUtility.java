package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.reporter.html.model.LinkName;
import org.jboss.windup.engine.visitor.reporter.html.model.Name;
import org.jboss.windup.engine.visitor.reporter.html.model.ReportContext;
import org.jboss.windup.engine.visitor.reporter.html.model.SimpleName;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.dao.SourceReportDao;
import org.jboss.windup.graph.model.ApplicationReferenceModel;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.JarManifestModel;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingUtility
{
    private static final Logger LOG = LoggerFactory.getLogger(NamingUtility.class);

    @Inject
    private ApplicationReferenceDao applicationReferenceDao;

    @Inject
    private SourceReportDao sourceReportDao;

    public String getApplicationName()
    {
        for (ApplicationReferenceModel appRef : applicationReferenceDao.getAll())
        {
            return StringUtils.defaultIfBlank(appRef.getArchive().getArchiveName(), "Unnamed");
        }
        return "Unknown";
    }

    protected String buildFullPath(ArchiveEntryResourceModel resource)
    {
        String path = resource.getArchiveEntry();

        ArchiveModel archive = resource.getArchive();
        while (archive != null)
        {
            // if (archive.getParentResource() instanceof ArchiveEntryResourceModel)
            // {
            // ArchiveEntryResourceModel parentEntry = (ArchiveEntryResourceModel) archive.getParentResource();
            // // prepend
            // path = parentEntry.getArchiveEntry() + "/" + path;
            // archive = archive.getParentArchive();
            // }
            // else if (archive.getParentResource() instanceof FileResourceModel)
            // {
            // path = archive.getArchiveName() + "/" + path;
            // archive = archive.getParentArchive();
            // }
        }
        return path;
    }

    protected String buildFullPath(ArchiveModel resource)
    {
        String path = resource.getArchiveName();

        ArchiveModel archive = resource;
        while (archive != null)
        {
            // if (archive.getParentResource() instanceof ArchiveEntryResourceModel)
            // {
            // ArchiveEntryResourceModel parentEntry = (ArchiveEntryResourceModel) archive.getParentResource();
            // // prepend
            // path = parentEntry.getArchiveEntry() + "/" + path;
            // archive = archive.getParentArchive();
            // }
            // else if (archive.getParentResource() instanceof FileResourceModel)
            // {
            // path = archive.getArchiveName() + "/" + path;
            // archive = archive.getParentArchive();
            // }
        }
        return path;
    }

    protected Name getReportJavaResource(File baseDirectory, File thisReport, JavaClassModel clz)
    {
        if (!sourceReportDao.hasSourceReport(clz))
        {
            return new SimpleName(clz.getQualifiedName());
        }

        FileResourceModel reportLocation = sourceReportDao.getResourceReport(clz);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(), clz.getQualifiedName());
        return linked;
    }

    protected Name getReportXmlResource(File baseDirectory, File thisReport, XmlResourceModel xml)
    {
        if (!sourceReportDao.hasSourceReport(xml))
        {
            return new SimpleName(getXmlResourceName(xml));
        }

        FileResourceModel reportLocation = sourceReportDao.getResourceReport(xml);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(), getXmlResourceName(xml));
        return linked;
    }

    protected Name getReportPropertiesResource(File baseDirectory, File thisReport, PropertiesMetaModel properties)
    {
        if (!sourceReportDao.hasSourceReport(properties))
        {
            return new SimpleName(getPropertiesResourceName(properties));
        }

        FileResourceModel reportLocation = sourceReportDao.getResourceReport(properties);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(),
                    getPropertiesResourceName(properties));
        return linked;
    }

    protected Name getReportManifestResource(File baseDirectory, File thisReport, JarManifestModel manifest)
    {
        if (!sourceReportDao.hasSourceReport(manifest))
        {
            return new SimpleName(getManifestResourceName(manifest));
        }

        FileResourceModel reportLocation = sourceReportDao.getResourceReport(manifest);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(),
                    getManifestResourceName(manifest));
        return linked;
    }

    protected String getPropertiesResourceName(PropertiesMetaModel entry)
    {
        if (entry.getResource() instanceof ArchiveEntryResourceModel)
        {
            ArchiveEntryResourceModel resource = (ArchiveEntryResourceModel) entry.getResource();
            return resource.getArchiveEntry();
        }
        else if (entry.getResource() instanceof FileResourceModel)
        {
            FileResourceModel resource = (FileResourceModel) entry.getResource();
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }

    protected String getManifestResourceName(JarManifestModel manifest)
    {
        if (manifest.getResource() instanceof ArchiveEntryResourceModel)
        {
            ArchiveEntryResourceModel resource = (ArchiveEntryResourceModel) manifest.getResource();
            return resource.getArchiveEntry();
        }
        else if (manifest.getResource() instanceof FileResourceModel)
        {
            FileResourceModel resource = (FileResourceModel) manifest.getResource();
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }

    protected String getXmlResourceName(XmlResourceModel xml)
    {
        if (xml.getResource() instanceof ArchiveEntryResourceModel)
        {
            ArchiveEntryResourceModel resource = (ArchiveEntryResourceModel) xml.getResource();
            return resource.getArchiveEntry();
        }
        else if (xml.getResource() instanceof FileResourceModel)
        {
            FileResourceModel resource = (FileResourceModel) xml.getResource();
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }
}
