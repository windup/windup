package org.apache.wicket.request.resource;

import java.util.*;

public class CssResourceReference extends PackageResourceReference{
    private static final long serialVersionUID=1L;
    public CssResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    public CssResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public IResource getResource(){
        return new CssPackageResource(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation());
    }
}
