package org.apache.wicket;

import java.util.*;
import org.apache.wicket.request.resource.*;

public class SharedResources{
    private final ResourceReferenceRegistry registry;
    public SharedResources(final ResourceReferenceRegistry registry){
        super();
        this.registry=registry;
    }
    public final void add(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final IResource resource){
        final ResourceReference ref=new AutoResourceReference((Class)scope,name,locale,style,variation,resource);
        this.registry.registerResourceReference(ref);
    }
    public final void add(final String name,final Locale locale,final IResource resource){
        this.add((Class<?>)Application.class,name,locale,null,null,resource);
    }
    public final void add(final String name,final IResource resource){
        this.add((Class<?>)Application.class,name,null,null,null,resource);
    }
    public ResourceReference get(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final boolean strict){
        return this.registry.getResourceReference(scope,name,locale,style,variation,strict,true);
    }
    public final ResourceReference remove(final ResourceReference.Key key){
        return this.registry.unregisterResourceReference(key);
    }
    private static final class AutoResourceReference extends ResourceReference{
        private static final long serialVersionUID=1L;
        private final IResource resource;
        private AutoResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final IResource resource){
            super(scope,name,locale,style,variation);
            this.resource=resource;
        }
        public IResource getResource(){
            return this.resource;
        }
    }
}
