package org.apache.wicket.request.mapper;

import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.*;
import org.apache.wicket.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.request.component.*;

public class PageInstanceMapper extends AbstractComponentMapper{
    public IRequestHandler mapRequest(final Request request){
        final Url url=request.getUrl();
        if(this.matches(url)){
            final PageComponentInfo info=this.getPageComponentInfo(url);
            if(info!=null&&info.getPageInfo().getPageId()!=null){
                final Integer renderCount=(info.getComponentInfo()!=null)?info.getComponentInfo().getRenderCount():null;
                if(info.getComponentInfo()==null){
                    final PageProvider provider=new PageProvider(info.getPageInfo().getPageId(),renderCount);
                    provider.setPageSource(this.getContext());
                    return (IRequestHandler)new RenderPageRequestHandler(provider);
                }
                final ComponentInfo componentInfo=info.getComponentInfo();
                final PageAndComponentProvider provider2=new PageAndComponentProvider(info.getPageInfo().getPageId(),renderCount,componentInfo.getComponentPath());
                provider2.setPageSource(this.getContext());
                final RequestListenerInterface listenerInterface=this.requestListenerInterfaceFromString(componentInfo.getListenerInterface());
                return (IRequestHandler)new ListenerInterfaceRequestHandler(provider2,listenerInterface,componentInfo.getBehaviorId());
            }
        }
        return null;
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        PageComponentInfo info=null;
        if(requestHandler instanceof RenderPageRequestHandler){
            final IRequestablePage page=((RenderPageRequestHandler)requestHandler).getPage();
            final PageInfo i=new PageInfo(page.getPageId());
            info=new PageComponentInfo(i,(ComponentInfo)null);
        }
        else if(requestHandler instanceof ListenerInterfaceRequestHandler){
            final ListenerInterfaceRequestHandler handler=(ListenerInterfaceRequestHandler)requestHandler;
            final IRequestablePage page2=handler.getPage();
            final String componentPath=handler.getComponentPath();
            final RequestListenerInterface listenerInterface=handler.getListenerInterface();
            Integer renderCount=null;
            if(listenerInterface.isIncludeRenderCount()){
                renderCount=page2.getRenderCount();
            }
            final PageInfo pageInfo=new PageInfo(page2.getPageId());
            final ComponentInfo componentInfo=new ComponentInfo(renderCount,this.requestListenerInterfaceToString(listenerInterface),componentPath,handler.getBehaviorIndex());
            info=new PageComponentInfo(pageInfo,componentInfo);
        }
        if(info!=null){
            final Url url=new Url();
            url.getSegments().add(this.getContext().getNamespace());
            url.getSegments().add(this.getContext().getPageIdentifier());
            this.encodePageComponentInfo(url,info);
            return url;
        }
        return null;
    }
    public int getCompatibilityScore(final Request request){
        int score=0;
        final Url url=request.getUrl();
        if(this.matches(url)){
            score=Integer.MAX_VALUE;
        }
        return score;
    }
    private boolean matches(final Url url){
        final IMapperContext mapperContext=this.getContext();
        return this.urlStartsWith(url,new String[] { mapperContext.getNamespace(),mapperContext.getPageIdentifier() });
    }
}
