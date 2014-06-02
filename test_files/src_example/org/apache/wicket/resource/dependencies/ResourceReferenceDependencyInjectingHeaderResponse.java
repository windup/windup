package org.apache.wicket.resource.dependencies;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.resource.*;

public class ResourceReferenceDependencyInjectingHeaderResponse extends DecoratingHeaderResponse{
    private final IResourceReferenceDependencyConfigurationService configurationService;
    public ResourceReferenceDependencyInjectingHeaderResponse(final IHeaderResponse decorated){
        this(decorated,null);
    }
    public ResourceReferenceDependencyInjectingHeaderResponse(final IHeaderResponse decorated,final IResourceReferenceDependencyConfigurationService configurator){
        super(decorated);
        this.configurationService=configurator;
    }
    public IResourceReferenceDependencyConfigurationService getConfigurationService(){
        if(this.configurationService==null){
            throw new IllegalStateException("you must either provide an IResourceReferenceDependencyConfigurationService at construction time or override getConfigurationService()");
        }
        return this.configurationService;
    }
    public void renderCSSReference(final ResourceReference reference){
        this.render(this.get(reference));
    }
    public void renderCSSReference(final ResourceReference reference,final String media){
        final AbstractResourceDependentResourceReference parent=this.get(reference);
        parent.setMedia(media);
        this.render(parent);
    }
    public void renderJavaScriptReference(final ResourceReference reference){
        this.render(this.get(reference));
    }
    public void renderJavaScriptReference(final ResourceReference reference,final String id){
        final AbstractResourceDependentResourceReference parent=this.get(reference);
        parent.setUniqueId(id);
        this.render(parent);
    }
    protected void render(final AbstractResourceDependentResourceReference parent){
        for(final AbstractResourceDependentResourceReference child : parent.getDependentResourceReferences()){
            this.render(child);
        }
        final boolean css=AbstractResourceDependentResourceReference.ResourceType.CSS.equals(parent.getResourceType());
        final String string=css?parent.getMedia():parent.getUniqueId();
        ResourceUtil.renderTo(this.getRealResponse(),parent,css,string);
    }
    private AbstractResourceDependentResourceReference get(final ResourceReference reference){
        final AbstractResourceDependentResourceReference ref=this.getConfigurationService().configure(reference);
        if(ref==null){
            throw new IllegalStateException("your IResourceReferenceDependencyConfigurationService can not return null from configure");
        }
        return ref;
    }
}
