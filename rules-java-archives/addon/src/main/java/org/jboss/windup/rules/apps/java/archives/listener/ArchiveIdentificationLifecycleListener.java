package org.jboss.windup.rules.apps.java.archives.listener;

import java.util.logging.Logger;
import javax.inject.Inject;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;

/**
 * Registers the {@link ArchiveIdentificationGraphChangedListener}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class ArchiveIdentificationLifecycleListener extends AbstractRuleLifecycleListener
{
    private static final Logger LOG = Logger.getLogger(ArchiveIdentificationLifecycleListener.class.getSimpleName());

    @Inject ArchiveIdentificationGraphChangedListener graphChangeListener;

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        LOG.info("Registered " + ArchiveIdentificationGraphChangedListener.class.getSimpleName()
                    + " - archives will be identified automatically.");
        graphChangeListener.setGraphContext(event.getGraphContext());
        event.getGraphContext().getGraph().addListener(graphChangeListener);
    }
}