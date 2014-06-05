package org.apache.wicket.markup.html.image;

import org.apache.wicket.model.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.ajax.*;

public class NonCachingImage extends Image{
    private static final long serialVersionUID=1L;
    public NonCachingImage(final String id,final IModel<?> model){
        super(id,model);
    }
    public NonCachingImage(final String id,final IResource imageResource){
        super(id,imageResource);
    }
    public NonCachingImage(final String id,final ResourceReference resourceReference,final PageParameters resourceParameters){
        super(id,resourceReference,resourceParameters);
    }
    public NonCachingImage(final String id,final ResourceReference resourceReference){
        super(id,resourceReference);
    }
    public NonCachingImage(final String id,final String string){
        super(id,string);
    }
    public NonCachingImage(final String id){
        super(id);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(AjaxRequestTarget.get()==null){
            this.addAntiCacheParameter(tag);
        }
    }
}
