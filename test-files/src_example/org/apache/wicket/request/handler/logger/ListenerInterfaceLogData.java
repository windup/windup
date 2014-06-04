package org.apache.wicket.request.handler.logger;

import org.apache.wicket.request.component.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.form.*;
import java.io.*;

public class ListenerInterfaceLogData extends PageLogData{
    private static final long serialVersionUID=1L;
    private final Class<? extends IRequestableComponent> componentClass;
    private final String componentPath;
    private final Integer behaviorIndex;
    private Class<? extends Behavior> behaviorClass;
    private final String interfaceName;
    private final String interfaceMethod;
    private Class<? extends IRequestableComponent> submittingComponentClass;
    private String submittingComponentPath;
    public ListenerInterfaceLogData(final IPageAndComponentProvider pageAndComponentProvider,final RequestListenerInterface listenerInterface,final Integer behaviorIndex){
        super(pageAndComponentProvider);
        this.componentClass=tryToGetComponentClass(pageAndComponentProvider);
        this.componentPath=tryToGetComponentPath(pageAndComponentProvider);
        this.behaviorIndex=behaviorIndex;
        if(behaviorIndex!=null&&this.componentClass!=null){
            try{
                this.behaviorClass=(Class<? extends Behavior>)pageAndComponentProvider.getComponent().getBehaviorById(behaviorIndex).getClass();
            }
            catch(Exception ignore){
                this.behaviorClass=null;
            }
        }
        else{
            this.behaviorClass=null;
        }
        this.interfaceName=listenerInterface.getName();
        this.interfaceMethod=listenerInterface.getMethod().getName();
        if(listenerInterface.getListenerInterfaceClass().equals(IFormSubmitListener.class)){
            final Component formSubmitter=tryToGetFormSubmittingComponent(pageAndComponentProvider);
            if(formSubmitter!=null){
                this.submittingComponentClass=(Class<? extends IRequestableComponent>)formSubmitter.getClass();
                this.submittingComponentPath=formSubmitter.getPageRelativePath();
            }
        }
    }
    private static Class<? extends IRequestableComponent> tryToGetComponentClass(final IPageAndComponentProvider pageAndComponentProvider){
        try{
            return (Class<? extends IRequestableComponent>)pageAndComponentProvider.getComponent().getClass();
        }
        catch(Exception e){
            return null;
        }
    }
    private static String tryToGetComponentPath(final IPageAndComponentProvider pageAndComponentProvider){
        try{
            return pageAndComponentProvider.getComponentPath();
        }
        catch(Exception e){
            return null;
        }
    }
    private static Component tryToGetFormSubmittingComponent(final IPageAndComponentProvider pageAndComponentProvider){
        try{
            final IRequestableComponent component=pageAndComponentProvider.getComponent();
            if(component instanceof Form){
                final IFormSubmitter submitter=((Form)component).findSubmittingButton();
                return (submitter instanceof Component)?submitter:null;
            }
            return null;
        }
        catch(Exception e){
            return null;
        }
    }
    public final Class<? extends IRequestableComponent> getComponentClass(){
        return this.componentClass;
    }
    public final String getComponentPath(){
        return this.componentPath;
    }
    public final Integer getBehaviorIndex(){
        return this.behaviorIndex;
    }
    public final Class<? extends Behavior> getBehaviorClass(){
        return this.behaviorClass;
    }
    public final String getInterfaceName(){
        return this.interfaceName;
    }
    public final String getInterfaceMethod(){
        return this.interfaceMethod;
    }
    public Class<? extends IRequestableComponent> getSubmittingComponentClass(){
        return this.submittingComponentClass;
    }
    public String getSubmittingComponentPath(){
        return this.submittingComponentPath;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder(super.toString());
        sb.setCharAt(sb.length()-1,',');
        if(this.getComponentClass()!=null){
            sb.append("componentClass=");
            sb.append(this.getComponentClass().getName());
            sb.append(',');
        }
        if(this.getComponentPath()!=null){
            sb.append("componentPath=");
            sb.append(this.getComponentPath());
            sb.append(',');
        }
        sb.append("behaviorIndex=");
        sb.append(this.getBehaviorIndex());
        if(this.getBehaviorClass()!=null){
            sb.append(",behaviorClass=");
            sb.append(this.getBehaviorClass().getName());
        }
        sb.append(",interfaceName=");
        sb.append(this.getInterfaceName());
        sb.append(",interfaceMethod=");
        sb.append(this.getInterfaceMethod());
        if(this.getSubmittingComponentClass()!=null){
            sb.append(",submittingComponentClass=");
            sb.append(this.getSubmittingComponentClass().getName());
        }
        if(this.getSubmittingComponentPath()!=null){
            sb.append(",submittingComponentPath=");
            sb.append(this.getSubmittingComponentPath());
        }
        sb.append("}");
        return sb.toString();
    }
}
