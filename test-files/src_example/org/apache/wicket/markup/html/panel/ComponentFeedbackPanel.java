package org.apache.wicket.markup.html.panel;

import org.apache.wicket.*;
import org.apache.wicket.feedback.*;

public class ComponentFeedbackPanel extends FeedbackPanel{
    private static final long serialVersionUID=1L;
    public ComponentFeedbackPanel(final String id,final Component filter){
        super(id,new ComponentFeedbackMessageFilter(filter));
    }
}
