package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.MavenFacetDao;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows Maven POM information.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class MavenPomReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(MavenPomReporter.class);

    @Inject
    private MavenFacetDao mavenDao;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }

    @Override
    public void run()
    {
        for (MavenFacetModel facet : mavenDao.getAll())
        {
            LOG.info("Maven: " + facet.getGroupId() + ":" + facet.getArtifactId() + ":" + facet.getVersion());

            if (facet.getParent() != null)
            {
                MavenFacetModel parent = facet.getParent();
                LOG.info(" - Parent: " + parent.getGroupId() + ":" + parent.getArtifactId() + ":" + parent.getVersion());
            }

            for (MavenFacetModel child : facet.getChildModules())
            {
                // report the xml files that contain the namespace...
                LOG.info(" - Module: " + child.getGroupId() + ":" + child.getArtifactId() + ":" + child.getVersion());
            }

            for (MavenFacetModel dep : facet.getDependencies())
            {
                // report the xml files that contain the namespace...
                LOG.info(" - Dependency: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion());
            }

        }
    }
}
