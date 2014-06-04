package org.apache.wicket.ajax.form;

import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.html.form.*;

public abstract class AjaxFormSubmitBehavior extends AjaxEventBehavior{
    private static final long serialVersionUID=1L;
    private Form<?> __form;
    private boolean defaultProcessing;
    public AjaxFormSubmitBehavior(final String event){
        this(null,event);
    }
    public AjaxFormSubmitBehavior(final Form<?> form,final String event){
        super(event);
        this.defaultProcessing=true;
        this.__form=form;
        if(form!=null){
            form.setOutputMarkupId(true);
        }
    }
    public final Form<?> getForm(){
        if(this.__form==null){
            this.__form=this.findForm();
            if(this.__form==null){
                throw new IllegalStateException("form was not specified in the constructor and cannot be found in the hierarchy of the component this behavior is attached to: Component="+this.getComponent().toString(false));
            }
        }
        return this.__form;
    }
    protected Form<?> findForm(){
        final Component component=this.getComponent();
        if(component instanceof Form){
            return (Form<?>)component;
        }
        return component.findParent((Class<Form<?>>)Form.class);
    }
    protected CharSequence getEventHandler(){
        final String formId=this.getForm().getMarkupId();
        final CharSequence url=this.getCallbackUrl();
        final AppendingStringBuffer call=new AppendingStringBuffer((CharSequence)"wicketSubmitFormById('").append(formId).append("', '").append((Object)url).append("', ");
        if(this.getComponent() instanceof IFormSubmittingComponent){
            call.append("'").append(((IFormSubmittingComponent)this.getComponent()).getInputName()).append("' ");
        }
        else{
            call.append("null");
        }
        return (CharSequence)((Object)this.generateCallbackScript((CharSequence)call)+";");
    }
    protected void onEvent(final AjaxRequestTarget target){
        this.getForm().getRootForm().onFormSubmitted(new IAfterFormSubmitter(){
            public Form<?> getForm(){
                return AjaxFormSubmitBehavior.this.getForm();
            }
            public boolean getDefaultFormProcessing(){
                return AjaxFormSubmitBehavior.this.getDefaultProcessing();
            }
            public void onSubmit(){
                AjaxFormSubmitBehavior.this.onSubmit(target);
            }
            public void onError(){
                AjaxFormSubmitBehavior.this.onError(target);
            }
            public void onAfterSubmit(){
                AjaxFormSubmitBehavior.this.onAfterSubmit(target);
            }
        });
    }
    protected void onAfterSubmit(final AjaxRequestTarget target){
    }
    protected void onSubmit(final AjaxRequestTarget target){
    }
    protected void onError(final AjaxRequestTarget target){
    }
    protected CharSequence getPreconditionScript(){
        return (CharSequence)("return Wicket.$$(this)&&Wicket.$$('"+this.getForm().getMarkupId()+"')");
    }
    public boolean getDefaultProcessing(){
        return this.defaultProcessing;
    }
    public void setDefaultProcessing(final boolean defaultProcessing){
        this.defaultProcessing=defaultProcessing;
    }
}
