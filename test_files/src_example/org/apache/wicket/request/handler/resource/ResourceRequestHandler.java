package org.apache.wicket.request.handler.resource;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;

public class ResourceRequestHandler implements IRequestHandler{
    private final IResource resource;
    private final PageParameters pageParameters;
    public ResourceRequestHandler(final IResource resource,final PageParameters pageParameters){
        super();
        Args.notNull((Object)resource,"resource");
        this.resource=resource;
        this.pageParameters=((pageParameters!=null)?pageParameters:new PageParameters());
    }
    public PageParameters getPageParameters(){
        return this.pageParameters;
    }
    public IResource getResource(){
        return this.resource;
    }
    public void respond(final IRequestCycle requestCycle){
        final IResource.Attributes a=new IResource.Attributes(requestCycle.getRequest(),requestCycle.getResponse(),this.pageParameters);
        this.resource.respond(a);
    }
    public void detach(final IRequestCycle requestCycle){
    }
}
