package org.apache.wicket.request.handler.resource;

import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.*;

public class ResourceReferenceRequestHandler implements IRequestHandler,ILoggableRequestHandler{
    private final ResourceReference resourceReference;
    private final PageParameters pageParameters;
    private ResourceReferenceLogData logData;
    public ResourceReferenceRequestHandler(final ResourceReference resourceReference){
        this(resourceReference,null);
    }
    public ResourceReferenceRequestHandler(final ResourceReference resourceReference,final PageParameters pageParameters){
        super();
        Args.notNull((Object)resourceReference,"resourceReference");
        this.resourceReference=resourceReference;
        this.pageParameters=((pageParameters!=null)?pageParameters:new PageParameters());
    }
    public ResourceReference getResourceReference(){
        return this.resourceReference;
    }
    public PageParameters getPageParameters(){
        return this.pageParameters;
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=new ResourceReferenceLogData(this);
        }
    }
    public ResourceReferenceLogData getLogData(){
        return this.logData;
    }
    public void respond(final IRequestCycle requestCycle){
        new ResourceRequestHandler(this.getResourceReference().getResource(),this.getPageParameters()).respond(requestCycle);
    }
    public Locale getLocale(){
        return this.getResourceReference().getLocale();
    }
    public IResource getResource(){
        return this.getResourceReference().getResource();
    }
    public String getStyle(){
        return this.getResourceReference().getStyle();
    }
    public String getVariation(){
        return this.getResourceReference().getVariation();
    }
}
