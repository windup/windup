package org.apache.wicket.request.mapper;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.util.lang.*;
import org.slf4j.*;

public abstract class AbstractBookmarkableMapper extends AbstractComponentMapper{
    private static Logger logger;
    protected abstract UrlInfo parseRequest(final Request p0);
    protected abstract Url buildUrl(final UrlInfo p0);
    protected abstract boolean pageMustHaveBeenCreatedBookmarkable();
    public abstract int getCompatibilityScore(final Request p0);
    protected IRequestHandler processBookmarkable(final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters){
        final PageProvider provider=new PageProvider(pageClass,pageParameters);
        provider.setPageSource(this.getContext());
        return (IRequestHandler)new RenderPageRequestHandler(provider);
    }
    protected IRequestHandler processHybrid(final PageInfo pageInfo,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters,final Integer renderCount){
        final PageProvider provider=new PageProvider(pageInfo.getPageId(),pageClass,pageParameters,renderCount);
        provider.setPageSource(this.getContext());
        return (IRequestHandler)new RenderPageRequestHandler(provider);
    }
    protected IRequestHandler processListener(final PageComponentInfo pageComponentInfo,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters){
        final PageInfo pageInfo=pageComponentInfo.getPageInfo();
        final ComponentInfo componentInfo=pageComponentInfo.getComponentInfo();
        Integer renderCount=null;
        RequestListenerInterface listenerInterface=null;
        if(componentInfo!=null){
            renderCount=componentInfo.getRenderCount();
            listenerInterface=this.requestListenerInterfaceFromString(componentInfo.getListenerInterface());
        }
        if(listenerInterface!=null){
            final PageAndComponentProvider provider=new PageAndComponentProvider(pageInfo.getPageId(),pageClass,pageParameters,renderCount,componentInfo.getComponentPath());
            provider.setPageSource(this.getContext());
            return (IRequestHandler)new ListenerInterfaceRequestHandler(provider,listenerInterface,componentInfo.getBehaviorId());
        }
        if(AbstractBookmarkableMapper.logger.isWarnEnabled()){
            if(componentInfo!=null){
                AbstractBookmarkableMapper.logger.warn("Unknown listener interface '{}'",componentInfo.getListenerInterface());
            }
            else{
                AbstractBookmarkableMapper.logger.warn("Cannot extract the listener interface for PageComponentInfo: '{}'"+pageComponentInfo);
            }
        }
        return null;
    }
    public IRequestHandler mapRequest(final Request request){
        final UrlInfo urlInfo=this.parseRequest(request);
        if(urlInfo!=null){
            final PageComponentInfo info=urlInfo.getPageComponentInfo();
            final Class<? extends IRequestablePage> pageClass=urlInfo.getPageClass();
            final PageParameters pageParameters=urlInfo.getPageParameters();
            if(info==null){
                return this.processBookmarkable(pageClass,pageParameters);
            }
            if(info.getPageInfo().getPageId()!=null&&info.getComponentInfo()==null){
                return this.processHybrid(info.getPageInfo(),pageClass,pageParameters,null);
            }
            if(info.getComponentInfo()!=null){
                return this.processListener(info,pageClass,pageParameters);
            }
            if(info.getPageInfo().getPageId()==null){
                return this.processBookmarkable(pageClass,pageParameters);
            }
        }
        return null;
    }
    protected boolean checkPageInstance(final IRequestablePage page){
        return page!=null&&this.checkPageClass((Class<? extends IRequestablePage>)page.getClass());
    }
    protected boolean checkPageClass(final Class<? extends IRequestablePage> pageClass){
        return true;
    }
    public Url mapHandler(IRequestHandler requestHandler){
        while(requestHandler instanceof IRequestHandlerDelegate){
            requestHandler=((IRequestHandlerDelegate)requestHandler).getDelegateHandler();
        }
        if(requestHandler instanceof BookmarkablePageRequestHandler){
            final BookmarkablePageRequestHandler handler=(BookmarkablePageRequestHandler)requestHandler;
            if(!this.checkPageClass(handler.getPageClass())){
                return null;
            }
            final PageInfo info=new PageInfo();
            final UrlInfo urlInfo=new UrlInfo(new PageComponentInfo(info,(ComponentInfo)null),handler.getPageClass(),handler.getPageParameters());
            return this.buildUrl(urlInfo);
        }
        else if(requestHandler instanceof RenderPageRequestHandler){
            final RenderPageRequestHandler handler2=(RenderPageRequestHandler)requestHandler;
            if(!this.checkPageClass(handler2.getPageClass())){
                return null;
            }
            if(handler2.getPageProvider().isNewPageInstance()){
                final PageInfo info=new PageInfo();
                final UrlInfo urlInfo=new UrlInfo(new PageComponentInfo(info,(ComponentInfo)null),handler2.getPageClass(),handler2.getPageParameters());
                return this.buildUrl(urlInfo);
            }
            final IRequestablePage page=handler2.getPage();
            if(this.checkPageInstance(page)&&(!this.pageMustHaveBeenCreatedBookmarkable()||page.wasCreatedBookmarkable())){
                final PageInfo info2=this.getPageInfo(handler2);
                final PageComponentInfo pageComponentInfo=(info2!=null)?new PageComponentInfo(info2,(ComponentInfo)null):null;
                final UrlInfo urlInfo2=new UrlInfo(pageComponentInfo,(Class<? extends IRequestablePage>)page.getClass(),handler2.getPageParameters());
                return this.buildUrl(urlInfo2);
            }
            return null;
        }
        else{
            if(!(requestHandler instanceof BookmarkableListenerInterfaceRequestHandler)){
                return null;
            }
            final BookmarkableListenerInterfaceRequestHandler handler3=(BookmarkableListenerInterfaceRequestHandler)requestHandler;
            final Class<? extends IRequestablePage> pageClass=handler3.getPageClass();
            if(!this.checkPageClass(pageClass)){
                return null;
            }
            Integer renderCount=null;
            if(handler3.getListenerInterface().isIncludeRenderCount()){
                renderCount=handler3.getRenderCount();
            }
            final PageInfo pageInfo=this.getPageInfo(handler3);
            final ComponentInfo componentInfo=new ComponentInfo(renderCount,this.requestListenerInterfaceToString(handler3.getListenerInterface()),handler3.getComponentPath(),handler3.getBehaviorIndex());
            final UrlInfo urlInfo3=new UrlInfo(new PageComponentInfo(pageInfo,componentInfo),pageClass,handler3.getPageParameters());
            return this.buildUrl(urlInfo3);
        }
    }
    protected final PageInfo getPageInfo(final IPageRequestHandler handler){
        Args.notNull((Object)handler,"handler");
        Integer pageId=null;
        if(handler.isPageInstanceCreated()){
            final IRequestablePage page=handler.getPage();
            if(!page.isPageStateless()){
                pageId=page.getPageId();
            }
        }
        return new PageInfo(pageId);
    }
    static{
        AbstractBookmarkableMapper.logger=LoggerFactory.getLogger(AbstractBookmarkableMapper.class);
    }
    protected static final class UrlInfo{
        private final PageComponentInfo pageComponentInfo;
        private final PageParameters pageParameters;
        private final Class<? extends IRequestablePage> pageClass;
        public UrlInfo(final PageComponentInfo pageComponentInfo,final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters){
            super();
            Args.notNull((Object)pageClass,"pageClass");
            this.pageComponentInfo=pageComponentInfo;
            this.pageParameters=this.cleanPageParameters(pageParameters);
            this.pageClass=pageClass;
        }
        private PageParameters cleanPageParameters(final PageParameters originalParameters){
            PageParameters cleanParameters=null;
            if(originalParameters!=null){
                cleanParameters=new PageParameters(originalParameters);
                cleanParameters.remove("wicket-ajax");
                cleanParameters.remove("wicket-ajax-baseurl");
                cleanParameters.remove("random");
                if(cleanParameters.isEmpty()){
                    cleanParameters=null;
                }
            }
            return cleanParameters;
        }
        public PageComponentInfo getPageComponentInfo(){
            return this.pageComponentInfo;
        }
        public Class<? extends IRequestablePage> getPageClass(){
            return this.pageClass;
        }
        public PageParameters getPageParameters(){
            return this.pageParameters;
        }
    }
}
