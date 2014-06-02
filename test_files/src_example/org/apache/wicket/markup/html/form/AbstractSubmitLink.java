package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;

public abstract class AbstractSubmitLink extends AbstractLink implements IFormSubmittingComponent,IAfterFormSubmitter{
    private static final long serialVersionUID=1L;
    private Form<?> form;
    private boolean defaultFormProcessing;
    public AbstractSubmitLink(final String id,final IModel<?> model){
        super(id,model);
        this.defaultFormProcessing=true;
    }
    public AbstractSubmitLink(final String id){
        super(id);
        this.defaultFormProcessing=true;
    }
    public AbstractSubmitLink(final String id,final IModel<?> model,final Form<?> form){
        super(id,model);
        this.defaultFormProcessing=true;
        this.form=form;
    }
    public AbstractSubmitLink(final String id,final Form<?> form){
        super(id);
        this.defaultFormProcessing=true;
        this.form=form;
    }
    public final AbstractSubmitLink setDefaultFormProcessing(final boolean defaultFormProcessing){
        if(this.defaultFormProcessing!=defaultFormProcessing){
            this.addStateChange();
        }
        this.defaultFormProcessing=defaultFormProcessing;
        return this;
    }
    public boolean getDefaultFormProcessing(){
        return this.defaultFormProcessing;
    }
    public Form<?> getForm(){
        if(this.form!=null){
            return this.form;
        }
        return this.findParent((Class<Form<?>>)Form.class);
    }
    public String getInputName(){
        return Form.getRootFormRelativeId(this);
    }
}
