package org.jboss.windup.engine.visitor.inspector;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.EarArchiveDao;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.dao.WarArchiveDao;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts manifest information to graph.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ManifestVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ManifestVisitor.class);

    @Inject
    private WindupContext context;

    @Inject
    private ArchiveEntryDao archiveEntryDao;

    @Inject
    private JarManifestDao jarManifestDao;
    
    @Inject
    private GraphUtil graphUtil;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        for (ArchiveEntryResource resource : archiveEntryDao.findArchiveEntry("META-INF/MANIFEST.MF"))
        {
            visitArchiveEntry(resource);
        }
        archiveEntryDao.commit();
    }

    @Override
    public void visitArchiveEntry(ArchiveEntryResource entry)
    {
        JarManifest jarManifest = jarManifestDao.create();
        JarArchive archive = graphUtil.castToType(entry.getArchive().asVertex(), JarArchive.class);
        jarManifest.setResource(entry);
        jarManifest.setJarArchive(archive);

        InputStream is = null;
        try
        {
            is = entry.asInputStream();
            Manifest manifest = new Manifest(entry.asInputStream());
            if (manifest == null || manifest.getMainAttributes().size() == 0)
            {
                return;
            }

            for (Object key : manifest.getMainAttributes().keySet())
            {
                String property = StringUtils.trim(key.toString());
                String propertyValue = StringUtils.trim(manifest.getMainAttributes().get(key).toString());
                jarManifest.setProperty(property, propertyValue);
            }
        }
        catch (Exception e)
        {
            LOG.warn("Exception reading manifest.", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

    }

}
