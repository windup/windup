package org.apache.wicket.ajax.form;

import org.apache.wicket.ajax.*;
import org.apache.wicket.feedback.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.behavior.*;

public class AjaxFormValidatingBehavior extends AjaxFormSubmitBehavior{
    private static final long serialVersionUID=1L;
    public AjaxFormValidatingBehavior(final Form<?> form,final String event){
        super(form,event);
    }
    protected void onSubmit(final AjaxRequestTarget target){
        this.addFeedbackPanels(target);
    }
    protected void onError(final AjaxRequestTarget target){
        this.addFeedbackPanels(target);
    }
    private void addFeedbackPanels(final AjaxRequestTarget target){
        this.getComponent().getPage().visitChildren((Class<?>)IFeedback.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                target.add(component);
            }
        });
    }
    public static void addToAllFormComponents(final Form<?> form,final String event){
        addToAllFormComponents(form,event,null);
    }
    public static void addToAllFormComponents(final Form<?> form,final String event,final Duration throttleDelay){
        form.visitChildren((Class<?>)FormComponent.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                final AjaxFormValidatingBehavior behavior=new AjaxFormValidatingBehavior(form,event);
                if(throttleDelay!=null){
                    behavior.setThrottleDelay(throttleDelay);
                }
                component.add(behavior);
                visit.dontGoDeeper();
            }
        });
    }
}
