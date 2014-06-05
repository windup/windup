package org.apache.wicket.feedback;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;

public class ContainerFeedbackMessageFilter implements IFeedbackMessageFilter{
    private static final long serialVersionUID=1L;
    private final MarkupContainer container;
    public ContainerFeedbackMessageFilter(final MarkupContainer container){
        super();
        if(container==null){
            throw new IllegalArgumentException("container must be not null");
        }
        this.container=container;
    }
    public boolean accept(final FeedbackMessage message){
        final Component reporter=message.getReporter();
        return reporter!=null&&(this.container.contains(reporter,true)||Objects.equal((Object)this.container,(Object)reporter));
    }
}
