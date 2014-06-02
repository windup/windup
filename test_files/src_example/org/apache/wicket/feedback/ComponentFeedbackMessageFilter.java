package org.apache.wicket.feedback;

import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class ComponentFeedbackMessageFilter implements IFeedbackMessageFilter{
    private static final long serialVersionUID=1L;
    private final Component component;
    public ComponentFeedbackMessageFilter(final Component component){
        super();
        this.component=component;
    }
    public boolean accept(final FeedbackMessage message){
        return Objects.equal((Object)this.component,(Object)message.getReporter());
    }
}
