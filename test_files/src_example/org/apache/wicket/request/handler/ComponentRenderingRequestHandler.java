package org.apache.wicket.request.handler;

import org.apache.wicket.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.http.*;

public class ComponentRenderingRequestHandler implements IComponentRequestHandler{
    private final Component component;
    public ComponentRenderingRequestHandler(final Component component){
        super();
        this.component=component;
    }
    public IRequestableComponent getComponent(){
        return this.component;
    }
    public void detach(final IRequestCycle requestCycle){
        this.component.getPage().detach();
    }
    public void respond(final IRequestCycle requestCycle){
        if(requestCycle.getResponse() instanceof WebResponse){
            final WebResponse response=(WebResponse)requestCycle.getResponse();
            response.disableCaching();
        }
        this.component.render();
    }
    public final String getComponentPath(){
        return this.component.getPageRelativePath();
    }
}
