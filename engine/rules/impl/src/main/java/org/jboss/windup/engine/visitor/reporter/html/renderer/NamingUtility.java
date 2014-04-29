package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.util.Iterator;

import javax.inject.Inject;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.engine.visitor.reporter.html.model.LinkName;
import org.jboss.windup.engine.visitor.reporter.html.model.Name;
import org.jboss.windup.engine.visitor.reporter.html.model.ReportContext;
import org.jboss.windup.engine.visitor.reporter.html.model.SimpleName;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.dao.SourceReportDao;
import org.jboss.windup.graph.model.meta.ApplicationReference;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.meta.PropertiesMeta;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingUtility
{
    private static final Logger LOG = LoggerFactory.getLogger(NamingUtility.class);

    @Inject
    private ApplicationReferenceDao applicationReferenceDao;

    @Inject
    private WindupContext context;

    @Inject
    private SourceReportDao sourceReportDao;
    
    @Inject
    private GraphUtil graphUtil;

    public String getApplicationName()
    {
        for( ApplicationReference appRef : applicationReferenceDao.getAll() ){
            if( appRef == null )  break;
            return StringUtils.defaultIfBlank( appRef.getArchive().getArchiveName(),  "Unnamed" );
        }
        return "Unknown";
    }

    protected String buildFullPath(ArchiveEntryResource resource)
    {
        String path = resource.getArchiveEntry();

        ArchiveResource archive = resource.getArchive();
        while (archive != null)
        {
            if (archive.getParentResource() instanceof ArchiveEntryResource)
            {
                ArchiveEntryResource parentEntry = graphUtil.castToType(archive.getParentResource().asVertex(), ArchiveEntryResource.class);
                // prepend
                path = parentEntry.getArchiveEntry() + "/" + path;
                archive = archive.getParentArchive();
            }
            else if (archive.getParentResource() instanceof FileResource)
            {
                path = archive.getArchiveName() + "/" + path;
                archive = archive.getParentArchive();
            }
        }
        return path;
    }

    protected String buildFullPath(ArchiveResource resource)
    {
        String path = resource.getArchiveName();

        ArchiveResource archive = resource;
        while (archive != null)
        {
            if (archive.getParentResource() instanceof ArchiveEntryResource)
            {
                ArchiveEntryResource parentEntry = graphUtil.castToType(archive.getParentResource().asVertex(), ArchiveEntryResource.class);
                // prepend
                path = parentEntry.getArchiveEntry() + "/" + path;
                archive = archive.getParentArchive();
            }
            else if (archive.getParentResource() instanceof FileResource)
            {
                path = archive.getArchiveName() + "/" + path;
                archive = archive.getParentArchive();
            }
        }
        return path;
    }

    protected Name getReportJavaResource(File baseDirectory, File thisReport, JavaClass clz)
    {
        if (!sourceReportDao.hasSourceReport(clz))
        {
            return new SimpleName(clz.getQualifiedName());
        }

        FileResource reportLocation = sourceReportDao.getResourceReport(clz);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(), clz.getQualifiedName());
        return linked;
    }

    protected Name getReportXmlResource(File baseDirectory, File thisReport, XmlResource xml)
    {
        if (!sourceReportDao.hasSourceReport(xml))
        {
            return new SimpleName(getXmlResourceName(xml));
        }

        FileResource reportLocation = sourceReportDao.getResourceReport(xml);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(), getXmlResourceName(xml));
        return linked;
    }

    protected Name getReportPropertiesResource(File baseDirectory, File thisReport, PropertiesMeta properties)
    {
        if (!sourceReportDao.hasSourceReport(properties))
        {
            return new SimpleName(getPropertiesResourceName(properties));
        }

        FileResource reportLocation = sourceReportDao.getResourceReport(properties);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(),
                    getPropertiesResourceName(properties));
        return linked;
    }

    protected Name getReportManifestResource(File baseDirectory, File thisReport, JarManifest manifest)
    {
        if (!sourceReportDao.hasSourceReport(manifest))
        {
            return new SimpleName(getManifestResourceName(manifest));
        }

        FileResource reportLocation = sourceReportDao.getResourceReport(manifest);

        ReportContext toBase = new ReportContext(baseDirectory, thisReport);
        ReportContext fromBase = new ReportContext(baseDirectory, reportLocation.asFile());

        Name linked = new LinkName(toBase.getRelativeFrom() + fromBase.getRelativeTo(),
                    getManifestResourceName(manifest));
        return linked;
    }

    protected String getPropertiesResourceName(PropertiesMeta entry)
    {
        if (entry.getResource() instanceof ArchiveEntryResource)
        {
            ArchiveEntryResource resource = graphUtil.castToType(entry.getResource().asVertex(), ArchiveEntryResource.class);
            return resource.getArchiveEntry();
        }
        else if (entry.getResource() instanceof FileResource)
        {
            FileResource resource = graphUtil.castToType(entry.getResource().asVertex(), FileResource.class);
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }

    protected String getManifestResourceName(JarManifest manifest)
    {
        if (manifest.getResource() instanceof ArchiveEntryResource)
        {
            ArchiveEntryResource resource = graphUtil.castToType(manifest.getResource().asVertex(), ArchiveEntryResource.class);
            return resource.getArchiveEntry();
        }
        else if (manifest.getResource() instanceof FileResource)
        {
            FileResource resource = graphUtil.castToType(manifest.getResource().asVertex(), FileResource.class);
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }

    protected String getXmlResourceName(XmlResource xml)
    {
        if (xml.getResource() instanceof ArchiveEntryResource)
        {
            ArchiveEntryResource resource = graphUtil.castToType(xml.getResource().asVertex(), ArchiveEntryResource.class);
            return resource.getArchiveEntry();
        }
        else if (xml.getResource() instanceof FileResource)
        {
            FileResource resource = graphUtil.castToType(xml.getResource().asVertex(), FileResource.class);
            return resource.getFilePath();
        }
        LOG.warn("Link is null.");
        return null;
    }
}
