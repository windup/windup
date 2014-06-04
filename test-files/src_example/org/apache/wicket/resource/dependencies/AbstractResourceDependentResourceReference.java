package org.apache.wicket.resource.dependencies;

import org.apache.wicket.request.resource.*;
import java.util.*;
import org.apache.wicket.util.string.*;

public abstract class AbstractResourceDependentResourceReference extends ResourceReference{
    private static final long serialVersionUID=1L;
    private String uniqueId;
    private String media;
    public AbstractResourceDependentResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    public AbstractResourceDependentResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public AbstractResourceDependentResourceReference(final String name){
        super(name);
    }
    public void setUniqueId(final String uniqueId){
        this.uniqueId=uniqueId;
    }
    public String getUniqueId(){
        return this.uniqueId;
    }
    public void setMedia(final String media){
        this.media=media;
    }
    public String getMedia(){
        return this.media;
    }
    public ResourceType getResourceType(){
        final String resourceName=this.getName();
        ResourceType type;
        if(Strings.isEmpty((CharSequence)resourceName)){
            type=ResourceType.PLAIN;
        }
        else if(resourceName.endsWith(".css")){
            type=ResourceType.CSS;
        }
        else{
            if(!resourceName.endsWith(".js")){
                throw new IllegalStateException("Cannot determine the resource's type by its name: "+resourceName);
            }
            type=ResourceType.JS;
        }
        return type;
    }
    public abstract AbstractResourceDependentResourceReference[] getDependentResourceReferences();
    public enum ResourceType{
        JS,CSS,PLAIN;
    }
}
