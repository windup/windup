package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.visit.*;

public class StatelessForm<T> extends Form<T>{
    private static final long serialVersionUID=1L;
    public StatelessForm(final String id){
        super(id);
    }
    public StatelessForm(final String id,final IModel<T> model){
        super(id,model);
    }
    protected boolean getStatelessHint(){
        return true;
    }
    protected MethodMismatchResponse onMethodMismatch(){
        this.setResponsePage((Class<IRequestablePage>)this.getPage().getClass(),this.getPage().getPageParameters());
        return MethodMismatchResponse.ABORT;
    }
    protected CharSequence getActionUrl(){
        return this.urlFor(IFormSubmitListener.INTERFACE,this.getPage().getPageParameters());
    }
    public void process(final IFormSubmitter submittingComponent){
        super.process(submittingComponent);
        final PageParameters parameters=this.getPage().getPageParameters();
        if(parameters!=null){
            this.visitFormComponents((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
                public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                    parameters.remove(formComponent.getInputName());
                }
            });
            parameters.remove(this.getHiddenFieldId());
            if(submittingComponent instanceof AbstractSubmitLink){
                final AbstractSubmitLink submitLink=(AbstractSubmitLink)submittingComponent;
                parameters.remove(submitLink.getInputName());
            }
        }
    }
}
