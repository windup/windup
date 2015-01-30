package org.jboss.windup.rules.apps.java.archives.listener;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;

/**
 * Registers the {@link ArchiveIdentificationGraphChangedListener}.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ArchiveIdentificationLifecycleListener extends AbstractRuleLifecycleListener
{
    private static final Logger LOG = Logger.getLogger(ArchiveIdentificationLifecycleListener.class.getSimpleName());

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        LOG.info("Registered " + ArchiveIdentificationGraphChangedListener.class.getSimpleName()
                    + "- Archives will be identified automatically.");
        event.getGraphContext().getGraph().addListener(new ArchiveIdentificationGraphChangedListener(event));
    }
}