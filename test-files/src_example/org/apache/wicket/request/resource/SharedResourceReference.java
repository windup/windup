package org.apache.wicket.request.resource;

import java.util.*;
import org.apache.wicket.*;

public class SharedResourceReference extends ResourceReference{
    private static final long serialVersionUID=1L;
    public SharedResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    public SharedResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public SharedResourceReference(final String name){
        super(name);
    }
    public IResource getResource(){
        final ResourceReference ref=Application.get().getResourceReferenceRegistry().getResourceReference(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation(),false,true);
        if(ref==null){
            return new AbstractResource(){
                private static final long serialVersionUID=1L;
                protected ResourceResponse newResourceResponse(final IResource.Attributes attributes){
                    final ResourceResponse res=new ResourceResponse();
                    res.setError(404);
                    return res;
                }
            };
        }
        if(ref!=this){
            return ref.getResource();
        }
        throw new IllegalStateException("SharedResourceReference can not be registered globally. See the documentation of this class.");
    }
    public boolean canBeRegistered(){
        return false;
    }
}
