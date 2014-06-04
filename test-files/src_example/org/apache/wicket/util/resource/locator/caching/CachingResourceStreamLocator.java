package org.apache.wicket.util.resource.locator.caching;

import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.lang.*;
import java.util.concurrent.*;
import java.util.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.resource.locator.*;

public class CachingResourceStreamLocator implements IResourceStreamLocator{
    private final ConcurrentMap<ResourceReference.Key,IResourceStreamReference> cache;
    private final IResourceStreamLocator delegate;
    public CachingResourceStreamLocator(final IResourceStreamLocator resourceStreamLocator){
        super();
        Args.notNull((Object)resourceStreamLocator,"resourceStreamLocator");
        this.delegate=resourceStreamLocator;
        this.cache=new ConcurrentHashMap<ResourceReference.Key,IResourceStreamReference>();
    }
    public IResourceStream locate(final Class<?> clazz,final String path){
        final ResourceReference.Key key=new ResourceReference.Key(clazz.getName(),path,null,null,null);
        final IResourceStreamReference resourceStreamReference=(IResourceStreamReference)this.cache.get(key);
        IResourceStream result;
        if(resourceStreamReference==null){
            result=this.delegate.locate(clazz,path);
            this.updateCache(key,result);
        }
        else{
            result=resourceStreamReference.getReference();
        }
        return result;
    }
    private void updateCache(final ResourceReference.Key key,final IResourceStream stream){
        if(null==stream){
            this.cache.put(key,NullResourceStreamReference.INSTANCE);
        }
        else if(stream instanceof FileResourceStream){
            final FileResourceStream fileResourceStream=(FileResourceStream)stream;
            this.cache.put(key,new FileResourceStreamReference(fileResourceStream));
        }
        else if(stream instanceof UrlResourceStream){
            final UrlResourceStream urlResourceStream=(UrlResourceStream)stream;
            this.cache.put(key,new UrlResourceStreamReference(urlResourceStream));
        }
    }
    public IResourceStream locate(final Class<?> scope,final String path,final String style,final String variation,final Locale locale,final String extension,final boolean strict){
        final ResourceReference.Key key=new ResourceReference.Key(scope.getName(),path,locale,style,variation);
        final IResourceStreamReference resourceStreamReference=(IResourceStreamReference)this.cache.get(key);
        IResourceStream result;
        if(resourceStreamReference==null){
            result=this.delegate.locate(scope,path,style,variation,locale,extension,strict);
            this.updateCache(key,result);
        }
        else{
            result=resourceStreamReference.getReference();
        }
        return result;
    }
    public ResourceNameIterator newResourceNameIterator(final String path,final Locale locale,final String style,final String variation,final String extension,final boolean strict){
        return this.delegate.newResourceNameIterator(path,locale,style,variation,extension,strict);
    }
}
