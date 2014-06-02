package org.apache.wicket.resource.filtering;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.resource.dependencies.*;
import org.apache.wicket.util.string.*;

public class CssAcceptingHeaderResponseFilter extends AbstractHeaderResponseFilter{
    public CssAcceptingHeaderResponseFilter(final String name){
        super(name);
    }
    public boolean acceptReference(final ResourceReference ref){
        if(ref instanceof AbstractResourceDependentResourceReference){
            return AbstractResourceDependentResourceReference.ResourceType.CSS.equals(((AbstractResourceDependentResourceReference)ref).getResourceType());
        }
        return !Strings.isEmpty((CharSequence)ref.getName())&&ref.getName().endsWith(".css");
    }
    public boolean acceptOtherJavaScript(){
        return false;
    }
}
