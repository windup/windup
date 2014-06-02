package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.markup.html.border.*;
import org.apache.wicket.feedback.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;

public class FormComponentFeedbackBorder extends Border implements IFeedback{
    private static final long serialVersionUID=1L;
    private boolean visible;
    public FormComponentFeedbackBorder(final String id){
        super(id);
        this.addToBorder(new ErrorIndicator("errorIndicator"));
    }
    protected void onBeforeRender(){
        super.onBeforeRender();
        this.visible=(Session.get().getFeedbackMessages().messages(this.getMessagesFilter()).size()!=0);
    }
    protected IFeedbackMessageFilter getMessagesFilter(){
        return new ContainerFeedbackMessageFilter(this);
    }
    private final class ErrorIndicator extends WebMarkupContainer{
        private static final long serialVersionUID=1L;
        public ErrorIndicator(final String id){
            super(id);
        }
        public boolean isVisible(){
            return FormComponentFeedbackBorder.this.visible;
        }
    }
}
