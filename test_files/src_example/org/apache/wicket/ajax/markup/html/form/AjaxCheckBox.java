package org.apache.wicket.ajax.markup.html.form;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.form.*;
import org.apache.wicket.ajax.*;

public abstract class AjaxCheckBox extends CheckBox{
    private static final long serialVersionUID=1L;
    public AjaxCheckBox(final String id){
        this(id,null);
    }
    public AjaxCheckBox(final String id,final IModel<Boolean> model){
        super(id,model);
        this.setOutputMarkupId(true);
        this.add(new AjaxFormComponentUpdatingBehavior("onclick"){
            private static final long serialVersionUID=1L;
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return AjaxCheckBox.this.getAjaxCallDecorator();
            }
            protected AjaxChannel getChannel(){
                return AjaxCheckBox.this.getChannel();
            }
            protected void onUpdate(final AjaxRequestTarget target){
                AjaxCheckBox.this.onUpdate(target);
            }
        });
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    protected abstract void onUpdate(final AjaxRequestTarget p0);
}
