package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.model.resource.JarArchiveModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each JAR, this displays the JARs that this JAR depends on. This is not transitive.
 * 
 * Example: Class A is in JAR W Class B is in JAR X Class C is in JAR Y Class D is in JAR Z
 * 
 * Class A extends Class B Class C implements Class D
 * 
 * JAR W Depends on JAR X JAR Y Depends on JAR Z
 * 
 * JAR X Provides for JAR W JAR Z Provides for JAR Y
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class ArchiveDependsOnReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveDependsOnReporter.class);

    @Inject
    private JarArchiveDao jarDao;
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }
    
    @Override
    public void run()
    {
        for (JarArchiveModel archive : jarDao.getAll())
        {
            LOG.info("Archive: " + archive.getArchiveName() + " - " + archive.asVertex());

            for (JarArchiveModel clz : archive.dependsOnArchives())
            {
                LOG.info(" - Depends On: " + clz.getArchiveName());
            }

            for (JarArchiveModel clz : archive.providesForArchives())
            {
                LOG.info(" - Provides For: " + clz.getArchiveName());
            }
            // look for circular...
            for (JarArchiveModel src : jarDao.findCircularReferences(archive))
            {
                LOG.info(" - Circular with: " + src.getArchiveName());
            }
        }
    }
}
