package org.jboss.windup.engine.visitor.inspector;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

/**
 * Goes through an archive adding the archive entries to the graph.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ArchiveEntryIndexVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(ArchiveEntryIndexVisitor.class);

    @Inject
    FileResourceDao fileDao;

    @Inject
    private ArchiveDao archiveDao;

    @Inject
    private ArchiveEntryDao archiveEntryDao;

    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return super.generateDependencies(BasicVisitor.class, ZipArchiveGraphVisitor.class);
    }

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.DISCOVERY;
    }

    @Override
    public void run()
    {
        final int total = (int) archiveDao.count(archiveDao.getAll());

        int i = 1;
        for (final ArchiveResource archive : archiveDao.getAll())
        {
            visitArchive(archive);
            LOG.info("Processed: " + i + " of " + total + " Archives.");
            i++;
        }
    }

    @Override
    public void visitArchive(ArchiveResource result)
    {
        Vertex v = result.asVertex();
        ArchiveResource file = archiveDao.getById(v.getId());

        ZipFile zipFileReference = null;
        try
        {
            File zipFile = archiveDao.asFile(result);
            zipFileReference = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zipFileReference.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory())
                {
                    continue;
                }
                // creates a new archive entry.
                ArchiveEntryResource resource = archiveEntryDao.create();
                resource.setArchiveEntry(entry.getName());
                resource.setArchive(file);
            }
        }
        catch (IOException e)
        {
            LOG.error("Exception while reading JAR.", e);
        }
        finally
        {
            org.apache.commons.io.IOUtils.closeQuietly(zipFileReference);
            archiveDao.commit();
        }
    }
}
