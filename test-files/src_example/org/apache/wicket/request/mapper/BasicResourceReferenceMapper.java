package org.apache.wicket.request.mapper;

import org.apache.wicket.util.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.caching.*;
import java.util.*;
import org.apache.wicket.request.resource.*;
import org.slf4j.*;
import java.io.*;

public class BasicResourceReferenceMapper extends AbstractResourceReferenceMapper{
    private static final Logger log;
    protected final IPageParametersEncoder pageParametersEncoder;
    protected final IProvider<? extends IResourceCachingStrategy> cachingStrategy;
    public BasicResourceReferenceMapper(final IPageParametersEncoder pageParametersEncoder,final IProvider<? extends IResourceCachingStrategy> cachingStrategy){
        super();
        this.pageParametersEncoder=(IPageParametersEncoder)Args.notNull((Object)pageParametersEncoder,"pageParametersEncoder");
        this.cachingStrategy=cachingStrategy;
    }
    public IRequestHandler mapRequest(final Request request){
        final Url url=request.getUrl();
        if(this.canBeHandled(url)){
            final int segmentsSize=url.getSegments().size();
            final PageParameters pageParameters=this.extractPageParameters(request,segmentsSize,this.pageParametersEncoder);
            final String className=(String)url.getSegments().get(2);
            final StringBuilder name=new StringBuilder(segmentsSize*2);
            for(int i=3;i<segmentsSize;++i){
                String segment=(String)url.getSegments().get(i);
                if(segment.indexOf(47)>-1){
                    return null;
                }
                if(i+1==segmentsSize&&!Strings.isEmpty((CharSequence)segment)){
                    final ResourceUrl resourceUrl=new ResourceUrl(segment,(INamedParameters)pageParameters);
                    this.getCachingStrategy().undecorateUrl(resourceUrl);
                    segment=resourceUrl.getFileName();
                    Checks.notEmpty(segment,"Caching strategy returned empty name for '%s'",new Object[] { resourceUrl });
                }
                if(name.length()>0){
                    name.append('/');
                }
                name.append(segment);
            }
            final ResourceReference.UrlAttributes attributes=this.getResourceReferenceAttributes(url);
            final Class<?> scope=this.resolveClass(className);
            if(scope!=null&&scope.getPackage()!=null){
                final ResourceReference res=this.getContext().getResourceReferenceRegistry().getResourceReference(scope,name.toString(),attributes.getLocale(),attributes.getStyle(),attributes.getVariation(),true,true);
                if(res!=null){
                    return (IRequestHandler)new ResourceReferenceRequestHandler(res,pageParameters);
                }
            }
        }
        return null;
    }
    protected final IResourceCachingStrategy getCachingStrategy(){
        return (IResourceCachingStrategy)this.cachingStrategy.get();
    }
    protected Class<?> resolveClass(final String name){
        return WicketObjects.resolveClass(name);
    }
    protected String getClassName(final Class<?> scope){
        return scope.getName();
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        if(requestHandler instanceof ResourceReferenceRequestHandler){
            final ResourceReferenceRequestHandler referenceRequestHandler=(ResourceReferenceRequestHandler)requestHandler;
            final ResourceReference reference=referenceRequestHandler.getResourceReference();
            if(reference instanceof MetaInfStaticResourceReference){
                final Url url=((MetaInfStaticResourceReference)reference).mapHandler((IRequestHandler)referenceRequestHandler);
                if(url!=null){
                    return url;
                }
            }
            if(reference.canBeRegistered()){
                final ResourceReferenceRegistry resourceReferenceRegistry=this.getContext().getResourceReferenceRegistry();
                resourceReferenceRegistry.registerResourceReference(reference);
            }
            Url url=new Url();
            final List<String> segments=(List<String>)url.getSegments();
            segments.add(this.getContext().getNamespace());
            segments.add(this.getContext().getResourceIdentifier());
            segments.add(this.getClassName(reference.getScope()));
            PageParameters parameters=referenceRequestHandler.getPageParameters();
            if(parameters==null){
                parameters=new PageParameters();
            }
            else{
                parameters=new PageParameters(parameters);
                parameters.clearIndexed();
            }
            this.encodeResourceReferenceAttributes(url,reference);
            final StringTokenizer tokens=new StringTokenizer(reference.getName(),"/");
            while(tokens.hasMoreTokens()){
                String token=tokens.nextToken();
                if(!tokens.hasMoreTokens()&&!Strings.isEmpty((CharSequence)token)){
                    final IResource resource=reference.getResource();
                    if(resource instanceof IStaticCacheableResource){
                        final IStaticCacheableResource cacheable=(IStaticCacheableResource)resource;
                        final ResourceUrl resourceUrl=new ResourceUrl(token,(INamedParameters)parameters);
                        this.getCachingStrategy().decorateUrl(resourceUrl,cacheable);
                        token=resourceUrl.getFileName();
                        Checks.notEmpty(token,"Caching strategy returned empty name for '%s'",new Object[] { resource });
                    }
                }
                segments.add(token);
            }
            if(!parameters.isEmpty()){
                url=this.encodePageParameters(url,parameters,this.pageParametersEncoder);
            }
            return url;
        }
        return null;
    }
    public int getCompatibilityScore(final Request request){
        final Url url=request.getUrl();
        int score=-1;
        if(this.canBeHandled(url)){
            score=1;
        }
        return score;
    }
    protected boolean canBeHandled(final Url url){
        return url.getSegments().size()>=4&&this.urlStartsWith(url,new String[] { this.getContext().getNamespace(),this.getContext().getResourceIdentifier() });
    }
    static{
        log=LoggerFactory.getLogger(BasicResourceReferenceMapper.class);
    }
}
