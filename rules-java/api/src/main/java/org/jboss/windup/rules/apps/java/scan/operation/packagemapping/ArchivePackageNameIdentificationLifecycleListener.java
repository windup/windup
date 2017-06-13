package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;

import java.util.logging.Logger;

/**
 * Registers the {@link ArchivePackageNameIdentificationGraphChangedListener}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jess Sightler</a>
 */
public class ArchivePackageNameIdentificationLifecycleListener extends AbstractRuleLifecycleListener
{
    private static final Logger LOG = Logger.getLogger(ArchivePackageNameIdentificationLifecycleListener.class.getName());

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        LOG.info("Registered " + ArchivePackageNameIdentificationGraphChangedListener.class.getSimpleName() + " - vendors will automatically be identified.");
        event.getGraphContext().getGraph().addListener(new ArchivePackageNameIdentificationGraphChangedListener(event));
    }
}
