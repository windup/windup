package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.JarManifestDao;
import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.resource.JarArchiveModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows all manifest properties.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class JarManifestReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(JarManifestReporter.class);

    @Inject
    private JarManifestDao manifestDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (JarManifestModel manifest : manifestDao.getAll())
        {
            JarArchiveModel archive = manifest.getJarArchive();

            LOG.info("Manifest for Archive: " + archive.getArchiveName());
            for (String key : manifest.keySet())
            {
                LOG.info("  - " + key + ": " + manifest.getProperty(key));
            }
        }
    }
}
