package org.apache.wicket.ajax.form;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.form.*;

public abstract class AjaxFormChoiceComponentUpdatingBehavior extends AbstractDefaultAjaxBehavior{
    private static final long serialVersionUID=1L;
    public void renderHead(final Component component,final IHeaderResponse response){
        super.renderHead(component,response);
        final AppendingStringBuffer asb=new AppendingStringBuffer();
        asb.append("function attachChoiceHandlers(markupId, callbackScript) {\n");
        asb.append(" var inputNodes = wicketGet(markupId).getElementsByTagName('input');\n");
        asb.append(" for (var i = 0 ; i < inputNodes.length ; i ++) {\n");
        asb.append(" var inputNode = inputNodes[i];\n");
        asb.append(" if (!inputNode.type) continue;\n");
        asb.append(" if (!(inputNode.className.indexOf('wicket-'+markupId)>=0)&&!(inputNode.id.indexOf(markupId+'-')>=0)) continue;\n");
        asb.append(" var inputType = inputNode.type.toLowerCase();\n");
        asb.append(" if (inputType == 'checkbox' || inputType == 'radio') {\n");
        asb.append(" Wicket.Event.add(inputNode, 'click', callbackScript);\n");
        asb.append(" }\n");
        asb.append(" }\n");
        asb.append("}\n");
        response.renderJavaScript((CharSequence)asb,"attachChoice");
        response.renderOnLoadJavaScript("attachChoiceHandlers('"+this.getComponent().getMarkupId()+"', function() {"+(Object)this.getEventHandler()+"});");
    }
    protected abstract void onUpdate(final AjaxRequestTarget p0);
    protected void onError(final AjaxRequestTarget target,final RuntimeException e){
        if(e!=null){
            throw e;
        }
    }
    protected void onBind(){
        super.onBind();
        if(!appliesTo(this.getComponent())){
            throw new WicketRuntimeException("Behavior "+this.getClass().getName()+" can only be added to an instance of a RadioChoice/CheckboxChoice/RadioGroup/CheckGroup");
        }
        if(this.getComponent() instanceof RadioGroup||this.getComponent() instanceof CheckGroup){
            this.getComponent().setRenderBodyOnly(false);
        }
    }
    protected final FormComponent<?> getFormComponent(){
        return (FormComponent<?>)this.getComponent();
    }
    protected final CharSequence getEventHandler(){
        return this.generateCallbackScript((CharSequence)new AppendingStringBuffer((CharSequence)"wicketAjaxPost('").append((Object)this.getCallbackUrl()).append("', wicketSerializeForm(document.getElementById('"+this.getComponent().getMarkupId()+"',false))"));
    }
    protected final void respond(final AjaxRequestTarget target){
        final FormComponent<?> formComponent=this.getFormComponent();
        try{
            formComponent.inputChanged();
            formComponent.validate();
            if(formComponent.hasErrorMessage()){
                formComponent.invalid();
                this.onError(target,null);
            }
            else{
                formComponent.valid();
                formComponent.updateModel();
                this.onUpdate(target);
            }
        }
        catch(RuntimeException e){
            this.onError(target,e);
        }
    }
    static boolean appliesTo(final Component component){
        return component instanceof RadioChoice||component instanceof CheckBoxMultipleChoice||component instanceof RadioGroup||component instanceof CheckGroup;
    }
}
