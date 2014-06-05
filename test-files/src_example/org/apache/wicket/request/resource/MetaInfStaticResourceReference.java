package org.apache.wicket.request.resource;

import org.apache.wicket.request.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.protocol.http.*;

public class MetaInfStaticResourceReference extends PackageResourceReference{
    private static final long serialVersionUID=-1858339228780709471L;
    private static Boolean META_INF_RESOURCES_SUPPORTED;
    public MetaInfStaticResourceReference(final Class<?> scope,final String name){
        super(scope,name);
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        if(!this.isMetaInfResourcesSupported()){
            return null;
        }
        final Url url=new Url();
        final List<String> segments=(List<String>)url.getSegments();
        String[] arr$;
        String[] parts=arr$=Packages.extractPackageName((Class)this.getScope()).split("\\.");
        for(final String p : arr$){
            segments.add(p);
        }
        parts=(arr$=this.getName().split("/"));
        for(final String p : arr$){
            segments.add(p);
        }
        return url;
    }
    protected boolean isMetaInfResourcesSupported(){
        if(MetaInfStaticResourceReference.META_INF_RESOURCES_SUPPORTED==null){
            final int majorVersion=WebApplication.get().getServletContext().getMajorVersion();
            MetaInfStaticResourceReference.META_INF_RESOURCES_SUPPORTED=(majorVersion>=3);
        }
        return MetaInfStaticResourceReference.META_INF_RESOURCES_SUPPORTED;
    }
    static{
        MetaInfStaticResourceReference.META_INF_RESOURCES_SUPPORTED=null;
    }
}
