package org.jboss.windup.config.operation.foreach;

import org.jboss.windup.config.operation.For;
import org.jboss.windup.config.operation.IterationRoot;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.IterationSelectionManager;
import org.ocpsoft.common.util.Assert;


/**
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public class ForImpl extends For implements IterationRoot
{
    private IterationPayloadManager payloadManager;
    private IterationSelectionManager selectionManager;

    
    @Override
    public void setSelectionManager(IterationSelectionManager selectionManager){
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
    public IterationSelectionManager getSelectionManager()
    {
        return selectionManager;
    }

    @Override
    public IterationPayloadManager getPayloadManager()
    {
        return payloadManager;
    }
}
