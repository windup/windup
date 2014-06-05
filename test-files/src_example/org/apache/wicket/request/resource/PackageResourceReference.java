package org.apache.wicket.request.resource;

import java.util.concurrent.*;
import java.util.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class PackageResourceReference extends ResourceReference{
    private static final long serialVersionUID=1L;
    private static final String CSS_EXTENSION="css";
    private static final String JAVASCRIPT_EXTENSION="js";
    private transient ConcurrentMap<UrlAttributes,UrlAttributes> urlAttributesCacheMap;
    public PackageResourceReference(final Key key){
        super(key);
    }
    public PackageResourceReference(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    public PackageResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public PackageResourceReference(final String name){
        super(name);
    }
    public IResource getResource(){
        final String extension=this.getExtension();
        if("css".equals(extension)){
            return new CssPackageResource(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation());
        }
        if("js".equals(extension)){
            return new JavaScriptPackageResource(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation());
        }
        return new PackageResource(this.getScope(),this.getName(),this.getLocale(),this.getStyle(),this.getVariation());
    }
    private UrlAttributes getUrlAttributes(final Locale locale,final String style,final String variation){
        final IResourceStreamLocator locator=Application.get().getResourceSettings().getResourceStreamLocator();
        final String absolutePath=Packages.absolutePath((Class)this.getScope(),this.getName());
        final IResourceStream stream=locator.locate(this.getScope(),absolutePath,style,variation,locale,null,false);
        if(stream==null){
            return new UrlAttributes(null,null,null);
        }
        return new UrlAttributes(stream.getLocale(),stream.getStyle(),stream.getVariation());
    }
    private Locale getCurrentLocale(){
        return (this.getLocale()!=null)?this.getLocale():Session.get().getLocale();
    }
    private String getCurrentStyle(){
        return (this.getStyle()!=null)?this.getStyle():Session.get().getStyle();
    }
    public UrlAttributes getUrlAttributes(){
        final Locale locale=this.getCurrentLocale();
        final String style=this.getCurrentStyle();
        final String variation=this.getVariation();
        final UrlAttributes key=new UrlAttributes(locale,style,variation);
        if(this.urlAttributesCacheMap==null){
            this.urlAttributesCacheMap=(ConcurrentMap<UrlAttributes,UrlAttributes>)Generics.newConcurrentHashMap();
        }
        UrlAttributes value=(UrlAttributes)this.urlAttributesCacheMap.get(key);
        if(value==null){
            value=this.getUrlAttributes(locale,style,variation);
            final UrlAttributes tmpValue=this.urlAttributesCacheMap.putIfAbsent(key,value);
            if(tmpValue!=null){
                value=tmpValue;
            }
        }
        return value;
    }
}
