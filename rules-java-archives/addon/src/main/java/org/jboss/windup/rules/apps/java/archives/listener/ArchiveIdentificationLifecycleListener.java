package org.jboss.windup.rules.apps.java.archives.listener;

import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Registers the {@link ArchiveIdentificationGraphChangedListener}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class ArchiveIdentificationLifecycleListener extends AbstractRuleLifecycleListener {
    private static final Logger LOG = Logger.getLogger(ArchiveIdentificationLifecycleListener.class.getName());

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Override
    public void beforeExecution(GraphRewrite event) {
        LOG.info("Registered " + ArchiveIdentificationGraphChangedListener.class.getSimpleName() + " - archives will be identified automatically.");
        event.getGraphContext().registerGraphListener(new ArchiveIdentificationGraphChangedListener(event.getGraphContext(), identifier));
    }
}
