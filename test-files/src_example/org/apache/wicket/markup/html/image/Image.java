package org.apache.wicket.markup.html.image;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.image.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.model.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.*;
import java.lang.reflect.*;

public class Image extends WebComponent implements IResourceListener{
    private static final long serialVersionUID=1L;
    private final LocalizedImageResource localizedImageResource;
    protected Image(final String id){
        super(id);
        this.localizedImageResource=new LocalizedImageResource(this);
    }
    public Image(final String id,final ResourceReference resourceReference){
        this(id,resourceReference,null);
    }
    public Image(final String id,final ResourceReference resourceReference,final PageParameters resourceParameters){
        super(id);
        this.localizedImageResource=new LocalizedImageResource(this);
        this.setImageResourceReference(resourceReference,resourceParameters);
    }
    public Image(final String id,final IResource imageResource){
        super(id);
        this.localizedImageResource=new LocalizedImageResource(this);
        this.setImageResource(imageResource);
    }
    public Image(final String id,final IModel<?> model){
        super(id,model);
        this.localizedImageResource=new LocalizedImageResource(this);
    }
    public Image(final String id,final String string){
        this(id,new Model<Object>(string));
    }
    public void onResourceRequested(){
        this.localizedImageResource.onResourceRequested(null);
    }
    public void setImageResource(final IResource imageResource){
        this.localizedImageResource.setResource(imageResource);
    }
    public void setImageResourceReference(final ResourceReference resourceReference){
        this.localizedImageResource.setResourceReference(resourceReference);
    }
    public void setImageResourceReference(final ResourceReference resourceReference,final PageParameters parameters){
        this.localizedImageResource.setResourceReference(resourceReference,parameters);
    }
    public Component setDefaultModel(final IModel<?> model){
        this.localizedImageResource.setResourceReference(null);
        this.localizedImageResource.setResource(null);
        return super.setDefaultModel(model);
    }
    protected IResource getImageResource(){
        return this.localizedImageResource.getResource();
    }
    protected ResourceReference getImageResourceReference(){
        return this.localizedImageResource.getResourceReference();
    }
    protected IModel<?> initModel(){
        return null;
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"img");
        super.onComponentTag(tag);
        final IResource resource=this.getImageResource();
        if(resource!=null){
            this.localizedImageResource.setResource(resource);
        }
        final ResourceReference resourceReference=this.getImageResourceReference();
        if(resourceReference!=null){
            this.localizedImageResource.setResourceReference(resourceReference);
        }
        this.localizedImageResource.setSrcAttribute(tag);
        if(this.shouldAddAntiCacheParameter()){
            this.addAntiCacheParameter(tag);
        }
    }
    protected boolean shouldAddAntiCacheParameter(){
        return AjaxRequestTarget.get()!=null;
    }
    protected final void addAntiCacheParameter(final ComponentTag tag){
        String url=tag.getAttributes().getString("src");
        url+=(url.contains((CharSequence)"?")?"&":"?");
        url=url+"antiCache="+System.currentTimeMillis();
        tag.put("src",(CharSequence)url);
    }
    protected boolean getStatelessHint(){
        return (this.getImageResource()==null||this.getImageResource()==this.localizedImageResource.getResource())&&this.localizedImageResource.isStateless();
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
    }
    public boolean canCallListenerInterface(final Method method){
        final boolean isResource=method!=null&&IResourceListener.class.isAssignableFrom(method.getDeclaringClass());
        return (isResource&&this.isVisibleInHierarchy())||super.canCallListenerInterface(method);
    }
}
