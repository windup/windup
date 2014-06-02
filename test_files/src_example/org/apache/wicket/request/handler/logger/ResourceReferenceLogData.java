package org.apache.wicket.request.handler.logger;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.resource.*;

public class ResourceReferenceLogData extends ResourceLogData{
    private static final long serialVersionUID=1L;
    private final Class<? extends ResourceReference> resourceReferenceClass;
    private final Class<?> scope;
    private final PageParameters pageParameters;
    public ResourceReferenceLogData(final ResourceReferenceRequestHandler refHandler){
        super(refHandler.getResourceReference().getName(),refHandler.getLocale(),refHandler.getStyle(),refHandler.getVariation());
        this.resourceReferenceClass=(Class<? extends ResourceReference>)refHandler.getResourceReference().getClass();
        this.scope=refHandler.getResourceReference().getScope();
        this.pageParameters=refHandler.getPageParameters();
    }
    public final Class<? extends ResourceReference> getResourceReferenceClass(){
        return this.resourceReferenceClass;
    }
    public final Class<?> getScope(){
        return this.scope;
    }
    public final PageParameters getPageParameters(){
        return this.pageParameters;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("{");
        this.fillToString(sb);
        sb.append(",resourceReferenceClass=");
        sb.append(this.getResourceReferenceClass().getName());
        sb.append(",scope=");
        sb.append((this.getScope()==null)?"null":this.getScope().getName());
        sb.append(",pageParameters={");
        sb.append(this.getPageParameters());
        sb.append("}}");
        return sb.toString();
    }
}
