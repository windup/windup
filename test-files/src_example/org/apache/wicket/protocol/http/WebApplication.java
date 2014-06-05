package org.apache.wicket.protocol.http;

import javax.servlet.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.mapper.*;
import org.apache.wicket.request.mapper.mount.*;
import org.apache.wicket.util.string.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.protocol.http.servlet.*;
import org.apache.wicket.util.watch.*;
import org.apache.wicket.markup.html.pages.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.*;
import org.apache.wicket.util.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.file.*;
import org.apache.wicket.request.*;
import org.slf4j.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.handler.render.*;
import org.apache.wicket.session.*;

public abstract class WebApplication extends Application{
    private static final Logger log;
    private ServletContext servletContext;
    private final AjaxRequestTargetListenerCollection ajaxRequestTargetListeners;
    private IContextProvider<AjaxRequestTarget,Page> ajaxRequestTargetProvider;
    private FilterFactoryManager filterFactoryManager;
    private RuntimeConfigurationType configurationType;
    private String sessionAttributePrefix;
    private WicketFilter wicketFilter;
    private final StoredResponsesMap storedResponses;
    public static WebApplication get(){
        final Application application=Application.get();
        if(!(application instanceof WebApplication)){
            throw new WicketRuntimeException("The application attached to the current thread is not a "+WebApplication.class.getSimpleName());
        }
        return (WebApplication)application;
    }
    public WebApplication(){
        super();
        this.storedResponses=new StoredResponsesMap(1000,Duration.seconds(60));
        this.ajaxRequestTargetListeners=new AjaxRequestTargetListenerCollection();
    }
    public final String getApplicationKey(){
        return this.getName();
    }
    public String getInitParameter(final String key){
        if(this.wicketFilter!=null){
            return this.wicketFilter.getFilterConfig().getInitParameter(key);
        }
        throw new IllegalStateException("init parameter '"+key+"' is not set yet. Any code in your"+" Application object that uses the wicketServlet/Filter instance should be put"+" in the init() method instead of your constructor");
    }
    public void setServletContext(final ServletContext servletContext){
        this.servletContext=servletContext;
    }
    public ServletContext getServletContext(){
        if(this.servletContext==null){
            throw new IllegalStateException("servletContext is not set yet. Any code in your Application object that uses the wicket filter instance should be put in the init() method instead of your constructor");
        }
        return this.servletContext;
    }
    public String getSessionAttributePrefix(final WebRequest request,String filterName){
        if(this.sessionAttributePrefix==null){
            if(filterName==null){
                filterName=this.getWicketFilter().getFilterConfig().getFilterName();
            }
            final String namespace=this.getMapperContext().getNamespace();
            this.sessionAttributePrefix=namespace+':'+filterName+':';
        }
        return this.sessionAttributePrefix;
    }
    public final WicketFilter getWicketFilter(){
        return this.wicketFilter;
    }
    public void logEventTarget(final IRequestHandler target){
        super.logEventTarget(target);
        final IRequestLogger rl=this.getRequestLogger();
        if(rl!=null){
            rl.logEventTarget(target);
        }
    }
    public void logResponseTarget(final IRequestHandler target){
        super.logResponseTarget(target);
        final IRequestLogger rl=this.getRequestLogger();
        if(rl!=null){
            rl.logResponseTarget(target);
        }
    }
    public final void mount(final IRequestMapper mapper){
        Args.notNull((Object)mapper,"mapper");
        this.getRootRequestMapperAsCompound().add(mapper);
    }
    public final <T extends Page> void mountPage(final String path,final Class<T> pageClass){
        this.mount((IRequestMapper)new MountedMapper(path,pageClass));
    }
    public final void mountResource(final String path,final ResourceReference reference){
        if(reference.canBeRegistered()){
            this.getResourceReferenceRegistry().registerResourceReference(reference);
        }
        this.mount((IRequestMapper)new ResourceMapper(path,reference));
    }
    public final <P extends Page> void mountPackage(final String path,final Class<P> pageClass){
        final PackageMapper packageMapper=new PackageMapper(PackageName.forClass((Class)pageClass));
        final MountMapper mountMapper=new MountMapper(path,(IRequestMapper)packageMapper);
        this.mount((IRequestMapper)mountMapper);
    }
    public final void unmount(String path){
        Args.notNull((Object)path,"path");
        if(path.charAt(0)=='/'){
            path=path.substring(1);
        }
        this.getRootRequestMapperAsCompound().unmount(path);
    }
    public final void addIgnoreMountPath(final String path){
        throw new UnsupportedOperationException();
    }
    protected WebRequest newWebRequest(final HttpServletRequest servletRequest,final String filterPath){
        return new ServletWebRequest(servletRequest,filterPath);
    }
    WebRequest createWebRequest(HttpServletRequest servletRequest,final String filterPath){
        if(servletRequest.getCharacterEncoding()==null){
            try{
                final String wicketAjaxHeader=servletRequest.getHeader("Wicket-Ajax");
                if(Strings.isTrue(wicketAjaxHeader)){
                    servletRequest.setCharacterEncoding("UTF-8");
                }
                else{
                    final String requestEncoding=this.getRequestCycleSettings().getResponseRequestEncoding();
                    servletRequest.setCharacterEncoding(requestEncoding);
                }
            }
            catch(UnsupportedEncodingException e){
                throw new WicketRuntimeException(e);
            }
        }
        if(this.hasFilterFactoryManager()){
            for(final AbstractRequestWrapperFactory factory : this.getFilterFactoryManager()){
                servletRequest=factory.getWrapper(servletRequest);
            }
        }
        final WebRequest webRequest=this.newWebRequest(servletRequest,filterPath);
        return webRequest;
    }
    protected WebResponse newWebResponse(final WebRequest webRequest,final HttpServletResponse httpServletResponse){
        return new ServletWebResponse((ServletWebRequest)webRequest,httpServletResponse);
    }
    WebResponse createWebResponse(final WebRequest webRequest,final HttpServletResponse httpServletResponse){
        final WebResponse webResponse=this.newWebResponse(webRequest,httpServletResponse);
        final boolean shouldBufferResponse=this.getRequestCycleSettings().getBufferResponse();
        return shouldBufferResponse?new HeaderBufferingWebResponse(webResponse):webResponse;
    }
    public Session newSession(final Request request,final Response response){
        return new WebSession(request);
    }
    public void sessionUnbound(final String sessionId){
        super.sessionUnbound(sessionId);
        final IRequestLogger logger=this.getRequestLogger();
        if(logger!=null){
            logger.sessionDestroyed(sessionId);
        }
    }
    public final void setWicketFilter(final WicketFilter wicketFilter){
        Args.notNull((Object)wicketFilter,"wicketFilter");
        this.wicketFilter=wicketFilter;
        this.servletContext=wicketFilter.getFilterConfig().getServletContext();
    }
    protected void init(){
        super.init();
    }
    public void internalDestroy(){
        final IModificationWatcher resourceWatcher=this.getResourceSettings().getResourceWatcher(false);
        if(resourceWatcher!=null){
            resourceWatcher.destroy();
        }
        final IFileCleaner fileCleaner=this.getResourceSettings().getFileCleaner();
        if(fileCleaner!=null){
            fileCleaner.destroy();
        }
        super.internalDestroy();
    }
    protected void internalInit(){
        super.internalInit();
        this.getApplicationSettings().setPageExpiredErrorPage((Class<? extends Page>)PageExpiredErrorPage.class);
        this.getApplicationSettings().setInternalErrorPage((Class<? extends Page>)InternalErrorPage.class);
        this.getApplicationSettings().setAccessDeniedPage((Class<? extends Page>)AccessDeniedPage.class);
        this.getPageSettings().addComponentResolver(new AutoLinkResolver());
        this.getPageSettings().addComponentResolver(new AutoLabelResolver());
        this.getPageSettings().addComponentResolver(new AutoLabelTextResolver());
        this.getResourceSettings().setResourceFinder(this.getResourceFinder());
        this.getResourceSettings().setFileCleaner((IFileCleaner)new FileCleaner());
        final String resourceFolder=this.getInitParameter("sourceFolder");
        if(resourceFolder!=null){
            this.getResourceSettings().addResourceFolder(resourceFolder);
        }
        this.setPageRendererProvider(new WebPageRendererProvider());
        this.setSessionStoreProvider((IProvider<ISessionStore>)new WebSessionStoreProvider());
        this.setAjaxRequestTargetProvider((IContextProvider<AjaxRequestTarget,Page>)new DefaultAjaxRequestTargetProvider());
        this.getAjaxRequestTargetListeners().add((Object)new AjaxEnclosureListener());
        this.configure();
    }
    public void setConfigurationType(final RuntimeConfigurationType configurationType){
        if(this.configurationType!=null){
            throw new IllegalStateException("Configuration type is write-once. You can not change it. Current value='"+configurationType);
        }
        this.configurationType=(RuntimeConfigurationType)Args.notNull((Object)configurationType,"configurationType");
    }
    public RuntimeConfigurationType getConfigurationType(){
        if(this.configurationType==null){
            String result=null;
            try{
                result=System.getProperty("wicket.configuration");
            }
            catch(SecurityException ex){
            }
            if(result==null){
                result=this.getInitParameter("wicket.configuration");
            }
            if(result==null){
                result=this.getServletContext().getInitParameter("wicket.configuration");
            }
            if(result==null){
                result=this.getInitParameter("configuration");
            }
            if(result==null){
                result=this.getServletContext().getInitParameter("configuration");
            }
            if(result!=null){
                try{
                    this.configurationType=RuntimeConfigurationType.valueOf(result.toUpperCase());
                }
                catch(IllegalArgumentException e){
                    throw new IllegalArgumentException("Invalid configuration type: '"+result+"'.  Must be \"development\" or \"deployment\".");
                }
            }
        }
        if(this.configurationType==null){
            this.configurationType=RuntimeConfigurationType.DEVELOPMENT;
        }
        return this.configurationType;
    }
    public void renderXmlDecl(final WebPage page,boolean insert){
        if(insert||"application/xhtml+xml".equalsIgnoreCase(page.getMarkupType().getMimeType())){
            final RequestCycle cycle=RequestCycle.get();
            if(!insert){
                final WebRequest request=(WebRequest)cycle.getRequest();
                final String accept=request.getHeader("Accept");
                insert=(accept==null||accept.indexOf("application/xhtml+xml")!=-1);
            }
            if(insert){
                final WebResponse response=(WebResponse)cycle.getResponse();
                response.write((CharSequence)"<?xml version='1.0'");
                final String encoding=this.getRequestCycleSettings().getResponseRequestEncoding();
                if(!Strings.isEmpty((CharSequence)encoding)){
                    response.write((CharSequence)" encoding='");
                    response.write((CharSequence)encoding);
                    response.write((CharSequence)"'");
                }
                response.write((CharSequence)" ?>");
            }
        }
    }
    protected IResourceFinder getResourceFinder(){
        return (IResourceFinder)new WebApplicationPath(this.getServletContext());
    }
    public final AjaxRequestTarget newAjaxRequestTarget(final Page page){
        final AjaxRequestTarget target=(AjaxRequestTarget)this.getAjaxRequestTargetProvider().get((Object)page);
        for(final AjaxRequestTarget.IListener listener : this.ajaxRequestTargetListeners){
            target.addListener(listener);
        }
        return target;
    }
    final void logStarted(){
        if(WebApplication.log.isInfoEnabled()){
            final String version=this.getFrameworkSettings().getVersion();
            final StringBuilder b=new StringBuilder();
            b.append("[").append(this.getName()).append("] Started Wicket ");
            if(!"n/a".equals(version)){
                b.append("version ").append(version).append(" ");
            }
            b.append("in ").append(this.getConfigurationType()).append(" mode");
            WebApplication.log.info(b.toString());
        }
        if(this.usesDevelopmentConfig()){
            this.outputDevelopmentModeWarning();
        }
    }
    protected void outputDevelopmentModeWarning(){
        System.err.print("********************************************************************\n*** WARNING: Wicket is running in DEVELOPMENT mode.              ***\n***                               ^^^^^^^^^^^                    ***\n*** Do NOT deploy to your live server(s) without changing this.  ***\n*** See Application#getConfigurationType() for more information. ***\n********************************************************************\n");
    }
    public boolean hasBufferedResponse(final String sessionId,final Url url){
        final String key=sessionId+url.toString();
        return this.storedResponses.containsKey((Object)key);
    }
    public BufferedWebResponse getAndRemoveBufferedResponse(final String sessionId,final Url url){
        final String key=sessionId+url.toString();
        return this.storedResponses.remove(key);
    }
    public void storeBufferedResponse(final String sessionId,final Url url,final BufferedWebResponse response){
        final String key=sessionId+url.toString();
        this.storedResponses.put(key,response);
    }
    public String getMimeType(final String fileName){
        final String mimeType=this.getServletContext().getMimeType(fileName);
        return (mimeType!=null)?mimeType:super.getMimeType(fileName);
    }
    public IContextProvider<AjaxRequestTarget,Page> getAjaxRequestTargetProvider(){
        return this.ajaxRequestTargetProvider;
    }
    public void setAjaxRequestTargetProvider(final IContextProvider<AjaxRequestTarget,Page> ajaxRequestTargetProvider){
        this.ajaxRequestTargetProvider=ajaxRequestTargetProvider;
    }
    public AjaxRequestTargetListenerCollection getAjaxRequestTargetListeners(){
        return this.ajaxRequestTargetListeners;
    }
    public final boolean hasFilterFactoryManager(){
        return this.filterFactoryManager!=null;
    }
    public final FilterFactoryManager getFilterFactoryManager(){
        if(this.filterFactoryManager==null){
            this.filterFactoryManager=new FilterFactoryManager();
        }
        return this.filterFactoryManager;
    }
    static{
        log=LoggerFactory.getLogger(WebApplication.class);
    }
    private static class WebPageRendererProvider implements IPageRendererProvider{
        public PageRenderer get(final RenderPageRequestHandler handler){
            return new WebPageRenderer(handler);
        }
    }
    private static class WebSessionStoreProvider implements IProvider<ISessionStore>{
        public ISessionStore get(){
            return new HttpSessionStore();
        }
    }
    private static class DefaultAjaxRequestTargetProvider implements IContextProvider<AjaxRequestTarget,Page>{
        public AjaxRequestTarget get(final Page context){
            return new AjaxRequestTarget(context);
        }
    }
}
