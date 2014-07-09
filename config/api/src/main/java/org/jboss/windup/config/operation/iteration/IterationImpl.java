package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.selectors.FramesSelector;
import org.ocpsoft.common.util.Assert;

public class IterationImpl extends Iteration
{
    private IterationPayloadManager payloadManager;
    private final FramesSelector selectionManager;

    public IterationImpl(FramesSelector selectionManager)
    {
        Assert.notNull(selectionManager, "Selection manager must not be null.");
        this.selectionManager = selectionManager;
    }

    @Override
    public void setPayloadManager(IterationPayloadManager payloadManager)
    {
        Assert.notNull(payloadManager, "Payload manager must not be null.");
        this.payloadManager = payloadManager;
    }

    @Override
    public FramesSelector getSelectionManager()
    {
        return selectionManager;
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }
}
