package org.jboss.windup.engine.visitor.inspector;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.ArchiveDao;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;

/**
 * Extracts the hash values from the archive and sets them on the archive vertex.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ArchiveHashVisitor extends AbstractGraphVisitor
{
    private static final Logger LOG = Logger.getLogger(ArchiveHashVisitor.class.getName());

    @Inject
    private FileResourceDao fileDao;

    @Inject
    private ArchiveDao archiveDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.INITIAL_ANALYSIS;
    }

    @Override
    public void run()
    {
        for (ArchiveResourceModel archive : archiveDao.getAll())
        {
            visitArchive(archive);
        }
        fileDao.commit();
    }

    @Override
    public void visitArchive(ArchiveResourceModel file)
    {
        InputStream is = null;
        try
        {
            is = file.asInputStream();
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);

            // start over
            is = file.asInputStream();
            String sha1 = org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);

            file.setSHA1Hash(sha1);
            file.setMD5Hash(md5);
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Exception generating hash.", e);
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

}
