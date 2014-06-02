package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.feedback.*;
import org.apache.wicket.*;

public class FormComponentFeedbackIndicator extends Panel implements IFeedback{
    private static final long serialVersionUID=1L;
    private IFeedbackMessageFilter filter;
    public FormComponentFeedbackIndicator(final String id){
        super(id);
    }
    public void setIndicatorFor(final Component component){
        this.filter=new ComponentFeedbackMessageFilter(component);
    }
    public void onConfigure(){
        super.onConfigure();
        this.setVisible(Session.get().getFeedbackMessages().hasMessage(this.getFeedbackMessageFilter()));
    }
    protected IFeedbackMessageFilter getFeedbackMessageFilter(){
        return this.filter;
    }
}
