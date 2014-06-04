package org.apache.wicket.ajax.form;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.*;
import org.slf4j.*;

public abstract class AjaxFormComponentUpdatingBehavior extends AjaxEventBehavior{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public AjaxFormComponentUpdatingBehavior(final String event){
        super(event);
    }
    protected void onBind(){
        super.onBind();
        if(!(this.getComponent() instanceof FormComponent)){
            throw new WicketRuntimeException("Behavior "+this.getClass().getName()+" can only be added to an instance of a FormComponent");
        }
        if(Application.get().usesDevelopmentConfig()&&AjaxFormChoiceComponentUpdatingBehavior.appliesTo(this.getComponent())){
            AjaxFormComponentUpdatingBehavior.log.warn(String.format("AjaxFormComponentUpdatingBehavior is not suposed to be added in the form component at path: \"%s\". Use the AjaxFormChoiceComponentUpdatingBehavior instead, that is meant for choices/groups that are not one component in the html but many",new Object[] { this.getComponent().getPageRelativePath() }));
        }
    }
    protected final FormComponent<?> getFormComponent(){
        return (FormComponent<?>)this.getComponent();
    }
    protected final CharSequence getEventHandler(){
        return this.generateCallbackScript((CharSequence)new AppendingStringBuffer((CharSequence)"wicketAjaxPost('").append((Object)this.getCallbackUrl()).append("', wicketSerialize(Wicket.$('"+this.getComponent().getMarkupId()+"'))"));
    }
    protected void onCheckEvent(final String event){
        if("href".equalsIgnoreCase(event)){
            throw new IllegalArgumentException("this behavior cannot be attached to an 'href' event");
        }
    }
    protected final void onEvent(final AjaxRequestTarget target){
        final FormComponent<?> formComponent=this.getFormComponent();
        if(this.getEvent().toLowerCase().equals("onblur")&&this.disableFocusOnBlur()){
            target.focusComponent(null);
        }
        try{
            formComponent.inputChanged();
            formComponent.validate();
            if(formComponent.hasErrorMessage()){
                formComponent.invalid();
                this.onError(target,null);
            }
            else{
                formComponent.valid();
                if(this.getUpdateModel()){
                    formComponent.updateModel();
                }
                this.onUpdate(target);
            }
        }
        catch(RuntimeException e){
            this.onError(target,e);
        }
    }
    protected boolean getUpdateModel(){
        return true;
    }
    protected boolean disableFocusOnBlur(){
        return true;
    }
    protected abstract void onUpdate(final AjaxRequestTarget p0);
    protected void onError(final AjaxRequestTarget target,final RuntimeException e){
        if(e!=null){
            throw e;
        }
    }
    static{
        log=LoggerFactory.getLogger(AjaxFormComponentUpdatingBehavior.class);
    }
}
