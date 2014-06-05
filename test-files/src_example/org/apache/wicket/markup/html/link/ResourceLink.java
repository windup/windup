package org.apache.wicket.markup.html.link;

import org.apache.wicket.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.request.*;

public class ResourceLink<T> extends Link<T> implements IResourceListener{
    private static final long serialVersionUID=1L;
    private final ResourceReference resourceReference;
    private final IResource resource;
    private final PageParameters resourceParameters;
    public ResourceLink(final String id,final ResourceReference resourceReference){
        this(id,resourceReference,null);
    }
    public ResourceLink(final String id,final ResourceReference resourceReference,final PageParameters resourceParameters){
        super(id);
        this.resourceReference=resourceReference;
        this.resourceParameters=resourceParameters;
        this.resource=null;
    }
    public ResourceLink(final String id,final IResource resource){
        super(id);
        this.resource=resource;
        this.resourceReference=null;
        this.resourceParameters=null;
    }
    public void onClick(){
    }
    public final void onResourceRequested(){
        final IResource.Attributes a=new IResource.Attributes(RequestCycle.get().getRequest(),RequestCycle.get().getResponse(),null);
        this.resource.respond(a);
        this.onLinkClicked();
    }
    protected final CharSequence getURL(){
        if(this.resourceReference!=null){
            if(this.resourceReference.canBeRegistered()){
                this.getApplication().getResourceReferenceRegistry().registerResourceReference(this.resourceReference);
            }
            return this.getRequestCycle().urlFor((IRequestHandler)new ResourceReferenceRequestHandler(this.resourceReference,this.resourceParameters));
        }
        return this.urlFor(IResourceListener.INTERFACE,this.resourceParameters);
    }
}
