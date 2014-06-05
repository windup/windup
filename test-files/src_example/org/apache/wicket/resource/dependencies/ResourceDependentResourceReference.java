package org.apache.wicket.resource.dependencies;

import java.util.*;
import org.apache.wicket.request.resource.*;

public class ResourceDependentResourceReference extends AbstractResourceDependentResourceReference{
    private static final long serialVersionUID=1L;
    private final AbstractResourceDependentResourceReference[] dependencies;
    public ResourceDependentResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation,final AbstractResourceDependentResourceReference[] dependencies){
        super(scope,name,locale,style,variation);
        this.dependencies=dependencies;
    }
    public ResourceDependentResourceReference(final Class<?> scope,final String name,final AbstractResourceDependentResourceReference[] dependencies){
        super(scope,name);
        this.dependencies=dependencies;
    }
    public ResourceDependentResourceReference(final String name,final AbstractResourceDependentResourceReference[] dependencies){
        super(name);
        this.dependencies=dependencies;
    }
    public final AbstractResourceDependentResourceReference[] getDependentResourceReferences(){
        return this.dependencies;
    }
    public IResource getResource(){
        return null;
    }
}
