package org.apache.wicket.request.resource;

import java.util.*;

public class JavaScriptResourceReference extends PackageResourceReference{
    private static final long serialVersionUID=1L;
    public JavaScriptResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    public JavaScriptResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public IResource getResource(){
        return new JavaScriptPackageResource(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation());
    }
}
