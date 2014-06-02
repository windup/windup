package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.image.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public class ImageButton extends Button implements IResourceListener{
    private static final long serialVersionUID=1L;
    private final LocalizedImageResource localizedImageResource;
    public ImageButton(final String id,final ResourceReference resourceReference){
        this(id,resourceReference,null);
    }
    public ImageButton(final String id,final ResourceReference resourceReference,final PageParameters resourceParameters){
        super(id);
        this.localizedImageResource=new LocalizedImageResource(this);
        this.setImageResourceReference(resourceReference,resourceParameters);
    }
    public ImageButton(final String id,final IResource imageResource){
        super(id);
        this.localizedImageResource=new LocalizedImageResource(this);
        this.setImageResource(imageResource);
    }
    public ImageButton(final String id,final IModel<String> model){
        super(id,model);
        this.localizedImageResource=new LocalizedImageResource(this);
    }
    public ImageButton(final String id,final String string){
        this(id,new Model<String>(string));
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
    public ImageButton setDefaultModel(final IModel<?> model){
        this.localizedImageResource.setResourceReference(null);
        this.localizedImageResource.setResource(null);
        return (ImageButton)super.setDefaultModel(model);
    }
    protected IResource getImageResource(){
        return this.localizedImageResource.getResource();
    }
    protected ResourceReference getImageResourceReference(){
        return this.localizedImageResource.getResourceReference();
    }
    protected final void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","image");
        final IResource resource=this.getImageResource();
        if(resource!=null){
            this.localizedImageResource.setResource(resource);
        }
        final ResourceReference resourceReference=this.getImageResourceReference();
        if(resourceReference!=null){
            this.localizedImageResource.setResourceReference(resourceReference);
        }
        this.localizedImageResource.setSrcAttribute(tag);
        super.onComponentTag(tag);
    }
    protected boolean getStatelessHint(){
        return this.getImageResource()==null&&this.localizedImageResource.isStateless();
    }
}
