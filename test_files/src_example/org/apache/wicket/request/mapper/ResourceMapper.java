package org.apache.wicket.request.mapper;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.request.mapper.parameter.*;
import java.util.*;
import org.apache.wicket.request.resource.*;

public class ResourceMapper extends AbstractMapper implements IRequestMapper{
    private final IPageParametersEncoder parametersEncoder;
    private final String[] mountSegments;
    private final ResourceReference resourceReference;
    public ResourceMapper(final String path,final ResourceReference resourceReference){
        this(path,resourceReference,(IPageParametersEncoder)new PageParametersEncoder());
    }
    public ResourceMapper(final String path,final ResourceReference resourceReference,final IPageParametersEncoder encoder){
        super();
        Args.notEmpty((CharSequence)path,"path");
        Args.notNull((Object)resourceReference,"resourceReference");
        Args.notNull((Object)encoder,"encoder");
        this.resourceReference=resourceReference;
        this.mountSegments=this.getMountSegments(path);
        this.parametersEncoder=encoder;
    }
    public IRequestHandler mapRequest(final Request request){
        final Url url=new Url(request.getUrl());
        PageParameters parameters=this.extractPageParameters(request,this.mountSegments.length,this.parametersEncoder);
        this.removeCachingDecoration(url,parameters);
        if(!this.urlStartsWith(url,this.mountSegments)){
            return null;
        }
        for(int index=0;index<this.mountSegments.length;++index){
            final String placeholder=this.getPlaceholder(this.mountSegments[index]);
            if(placeholder!=null){
                if(parameters==null){
                    parameters=new PageParameters();
                }
                parameters.add(placeholder,url.getSegments().get(index));
            }
        }
        return (IRequestHandler)new ResourceReferenceRequestHandler(this.resourceReference,parameters);
    }
    public int getCompatibilityScore(final Request request){
        return 0;
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        if(!(requestHandler instanceof ResourceReferenceRequestHandler)){
            return null;
        }
        final ResourceReferenceRequestHandler handler=(ResourceReferenceRequestHandler)requestHandler;
        if(!this.resourceReference.equals(handler.getResourceReference())){
            return null;
        }
        final Url url=new Url();
        for(final String segment : this.mountSegments){
            url.getSegments().add(segment);
        }
        final PageParameters parameters=new PageParameters(handler.getPageParameters());
        for(int index=0;index<this.mountSegments.length;++index){
            final String placeholder=this.getPlaceholder(this.mountSegments[index]);
            if(placeholder!=null){
                url.getSegments().set(index,parameters.get(placeholder).toString(""));
                parameters.remove(placeholder);
            }
        }
        this.addCachingDecoration(url,parameters);
        return this.encodePageParameters(url,parameters,this.parametersEncoder);
    }
    protected IResourceCachingStrategy getCachingStrategy(){
        return Application.get().getResourceSettings().getCachingStrategy();
    }
    protected void addCachingDecoration(final Url url,final PageParameters parameters){
        final List<String> segments=(List<String>)url.getSegments();
        final int lastSegmentAt=segments.size()-1;
        final String filename=(String)segments.get(lastSegmentAt);
        if(!Strings.isEmpty((CharSequence)filename)){
            final IResource resource=this.resourceReference.getResource();
            if(resource instanceof IStaticCacheableResource){
                final IStaticCacheableResource cacheable=(IStaticCacheableResource)resource;
                final ResourceUrl cacheUrl=new ResourceUrl(filename,(INamedParameters)parameters);
                this.getCachingStrategy().decorateUrl(cacheUrl,cacheable);
                if(Strings.isEmpty((CharSequence)cacheUrl.getFileName())){
                    throw new IllegalStateException("caching strategy returned empty name for "+resource);
                }
                segments.set(lastSegmentAt,cacheUrl.getFileName());
            }
        }
    }
    protected void removeCachingDecoration(final Url url,final PageParameters parameters){
        final List<String> segments=(List<String>)url.getSegments();
        if(!segments.isEmpty()){
            final int lastSegmentAt=segments.size()-1;
            final String filename=(String)segments.get(lastSegmentAt);
            if(Strings.isEmpty((CharSequence)filename)){
                return;
            }
            final ResourceUrl resourceUrl=new ResourceUrl(filename,(INamedParameters)parameters);
            this.getCachingStrategy().undecorateUrl(resourceUrl);
            if(Strings.isEmpty((CharSequence)resourceUrl.getFileName())){
                throw new IllegalStateException("caching strategy returned empty name for "+resourceUrl);
            }
            segments.set(lastSegmentAt,resourceUrl.getFileName());
        }
    }
}
