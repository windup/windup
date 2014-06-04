package org.apache.wicket.ajax.form;

import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.form.*;

public abstract class OnChangeAjaxBehavior extends AjaxFormComponentUpdatingBehavior{
    private static final long serialVersionUID=1L;
    public OnChangeAjaxBehavior(){
        super("onchange");
    }
    public void renderHead(final Component component,final IHeaderResponse response){
        super.renderHead(component,response);
        if(component instanceof AbstractTextComponent){
            final String id=this.getComponent().getMarkupId();
            response.renderOnDomReadyJavaScript("new Wicket.ChangeHandler('"+id+"');");
        }
    }
}
