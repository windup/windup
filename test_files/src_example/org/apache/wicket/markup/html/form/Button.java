package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class Button extends FormComponent<String> implements IFormSubmitter,IFormSubmittingComponent,IAfterFormSubmitter{
    private static final long serialVersionUID=1L;
    private boolean defaultFormProcessing;
    public Button(final String id){
        this(id,null);
    }
    public Button(final String id,final IModel<String> model){
        super(id,model);
        this.setVersioned(this.defaultFormProcessing=true);
        this.setOutputMarkupId(true);
        this.setEscapeModelStrings(false);
    }
    protected IModel<String> initModel(){
        return null;
    }
    public Form<?> getForm(){
        return Form.findForm(this);
    }
    public final boolean getDefaultFormProcessing(){
        return this.defaultFormProcessing;
    }
    public final Button setDefaultFormProcessing(final boolean defaultFormProcessing){
        if(this.defaultFormProcessing!=defaultFormProcessing){
            this.addStateChange();
        }
        this.defaultFormProcessing=defaultFormProcessing;
        return this;
    }
    public void updateModel(){
    }
    protected String getOnClickScript(){
        return null;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        try{
            final String value=this.getDefaultModelObjectAsString();
            if(value!=null&&!"".equals(value)){
                tag.put("value",(CharSequence)value);
            }
        }
        catch(Exception ex){
        }
        final String onClickJavaScript=this.getOnClickScript();
        if(onClickJavaScript!=null){
            tag.put("onclick",(CharSequence)onClickJavaScript);
        }
    }
    public void onSubmit(){
    }
    public void onError(){
    }
    public void onAfterSubmit(){
    }
}
