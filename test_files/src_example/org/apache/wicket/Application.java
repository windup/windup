package org.apache.wicket;

import org.apache.wicket.util.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.page.*;
import org.apache.wicket.application.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.response.filter.*;
import org.apache.wicket.javascript.*;
import org.apache.wicket.util.io.*;
import java.util.*;
import java.io.*;
import org.apache.wicket.request.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.markup.html.image.resource.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.mapper.*;
import org.apache.wicket.util.lang.*;
import java.net.*;
import org.apache.wicket.event.*;
import org.apache.wicket.settings.def.*;
import org.apache.wicket.session.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.markup.html.*;
import org.slf4j.*;

public abstract class Application implements ISessionStore.UnboundListener,IEventSink{
    public static final String CONFIGURATION="configuration";
    private static final Map<String,Application> applicationKeyToApplication;
    private static final Logger log;
    private IRequestMapper rootRequestMapper;
    private IConverterLocator converterLocator;
    private final List<IInitializer> initializers;
    private MetaDataEntry<?>[] metaData;
    private String name;
    private IRequestLogger requestLogger;
    private volatile ISessionStore sessionStore;
    private IPageRendererProvider pageRendererProvider;
    private IRequestCycleProvider requestCycleProvider;
    private IProvider<IExceptionMapper> exceptionMapperProvider;
    private IProvider<ISessionStore> sessionStoreProvider;
    private IHeaderResponseDecorator headerResponseDecorator;
    private final ComponentOnBeforeRenderListenerCollection componentPreOnBeforeRenderListeners;
    private final ComponentOnBeforeRenderListenerCollection componentPostOnBeforeRenderListeners;
    private final ComponentOnAfterRenderListenerCollection componentOnAfterRenderListeners;
    private final RequestCycleListenerCollection requestCycleListeners;
    private final ApplicationListenerCollection applicationListeners;
    private final SessionListenerCollection sessionListeners;
    private final ComponentInstantiationListenerCollection componentInstantiationListeners;
    private final ComponentInitializationListenerCollection componentInitializationListeners;
    private final HeaderContributorListenerCollection headerContributorListenerCollection;
    private final BehaviorInstantiationListenerCollection behaviorInstantiationListeners;
    private IApplicationSettings applicationSettings;
    private IDebugSettings debugSettings;
    private IExceptionSettings exceptionSettings;
    private IFrameworkSettings frameworkSettings;
    private IMarkupSettings markupSettings;
    private IPageSettings pageSettings;
    private IRequestCycleSettings requestCycleSettings;
    private IRequestLoggerSettings requestLoggerSettings;
    private IResourceSettings resourceSettings;
    private ISecuritySettings securitySettings;
    private ISessionSettings sessionSettings;
    private IStoreSettings storeSettings;
    private boolean settingsAccessible;
    private volatile IPageManager pageManager;
    private IPageManagerProvider pageManagerProvider;
    private final IPageManagerContext pageManagerContext;
    private ResourceReferenceRegistry resourceReferenceRegistry;
    private SharedResources sharedResources;
    private IPageFactory pageFactory;
    private IMapperContext encoderContext;
    public static boolean exists(){
        return ThreadContext.getApplication()!=null;
    }
    public static Application get(){
        final Application application=ThreadContext.getApplication();
        if(application==null){
            throw new WicketRuntimeException("There is no application attached to current thread "+Thread.currentThread().getName());
        }
        return application;
    }
    public static Application get(final String applicationKey){
        return (Application)Application.applicationKeyToApplication.get(applicationKey);
    }
    public static Set<String> getApplicationKeys(){
        return (Set<String>)Collections.unmodifiableSet(Application.applicationKeyToApplication.keySet());
    }
    public Application(){
        super();
        this.initializers=(List<IInitializer>)Generics.newArrayList();
        this.componentPreOnBeforeRenderListeners=new ComponentOnBeforeRenderListenerCollection();
        this.componentPostOnBeforeRenderListeners=new ComponentOnBeforeRenderListenerCollection();
        this.componentOnAfterRenderListeners=new ComponentOnAfterRenderListenerCollection();
        this.requestCycleListeners=new RequestCycleListenerCollection();
        this.applicationListeners=new ApplicationListenerCollection();
        this.sessionListeners=new SessionListenerCollection();
        this.componentInstantiationListeners=new ComponentInstantiationListenerCollection();
        this.componentInitializationListeners=new ComponentInitializationListenerCollection();
        this.headerContributorListenerCollection=new HeaderContributorListenerCollection();
        this.behaviorInstantiationListeners=new BehaviorInstantiationListenerCollection();
        this.pageManagerContext=new DefaultPageManagerContext();
        this.getComponentInstantiationListeners().add((Object)new IComponentInstantiationListener(){
            public void onInstantiation(final Component component){
                final Class<? extends Component> cl=(Class<? extends Component>)component.getClass();
                if(!Session.get().getAuthorizationStrategy().isInstantiationAuthorized(cl)){
                    Application.this.getSecuritySettings().getUnauthorizedComponentInstantiationListener().onUnauthorizedInstantiation(component);
                }
            }
        });
    }
    public final void configure(){
        switch(this.getConfigurationType()){
            case DEVELOPMENT:{
                this.getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
                this.getResourceSettings().setJavaScriptCompressor(null);
                this.getMarkupSettings().setStripWicketTags(false);
                this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_EXCEPTION_PAGE);
                this.getDebugSettings().setComponentUseCheck(true);
                this.getDebugSettings().setAjaxDebugModeEnabled(true);
                this.getDebugSettings().setDevelopmentUtilitiesEnabled(true);
                this.getRequestCycleSettings().addResponseFilter(EmptySrcAttributeCheckFilter.INSTANCE);
                break;
            }
            case DEPLOYMENT:{
                this.getResourceSettings().setResourcePollFrequency(null);
                this.getResourceSettings().setJavaScriptCompressor(new DefaultJavaScriptCompressor());
                this.getMarkupSettings().setStripWicketTags(true);
                this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
                this.getDebugSettings().setComponentUseCheck(false);
                this.getDebugSettings().setAjaxDebugModeEnabled(false);
                this.getDebugSettings().setDevelopmentUtilitiesEnabled(false);
                break;
            }
        }
    }
    public abstract String getApplicationKey();
    public abstract RuntimeConfigurationType getConfigurationType();
    public abstract Class<? extends Page> getHomePage();
    public final IConverterLocator getConverterLocator(){
        return this.converterLocator;
    }
    public final <T> T getMetaData(final MetaDataKey<T> key){
        return key.get(this.metaData);
    }
    public final String getName(){
        return this.name;
    }
    public final IRequestLogger getRequestLogger(){
        if(this.getRequestLoggerSettings().isRequestLoggerEnabled()){
            if(this.requestLogger==null){
                this.requestLogger=this.newRequestLogger();
            }
        }
        else{
            this.requestLogger=null;
        }
        return this.requestLogger;
    }
    public final ISessionStore getSessionStore(){
        if(this.sessionStore==null){
            synchronized(this){
                if(this.sessionStore==null){
                    (this.sessionStore=(ISessionStore)this.sessionStoreProvider.get()).registerUnboundListener(this);
                }
            }
        }
        return this.sessionStore;
    }
    public void sessionUnbound(final String sessionId){
        this.internalGetPageManager().sessionExpired(sessionId);
    }
    public final void initializeComponents(){
        try{
            final Iterator<URL> resources=this.getApplicationSettings().getClassResolver().getResources("wicket.properties");
            while(resources.hasNext()){
                InputStream in=null;
                try{
                    final URL url=(URL)resources.next();
                    final Properties properties=new Properties();
                    in=Streams.readNonCaching(url);
                    properties.load(in);
                    this.load(properties);
                }
                finally{
                    IOUtils.close((Closeable)in);
                }
            }
        }
        catch(IOException e){
            throw new WicketRuntimeException("Unable to load initializers file",e);
        }
        this.callInitializers();
    }
    public void logEventTarget(final IRequestHandler target){
    }
    public void logResponseTarget(final IRequestHandler requestTarget){
    }
    public abstract Session newSession(final Request p0,final Response p1);
    public final synchronized <T> void setMetaData(final MetaDataKey<T> key,final Object object){
        this.metaData=key.set(this.metaData,object);
    }
    private void addInitializer(final String className){
        final IInitializer initializer=(IInitializer)WicketObjects.newInstance(className);
        if(initializer!=null){
            this.initializers.add(initializer);
        }
    }
    private void callDestroyers(){
        for(final IInitializer initializer : this.initializers){
            Application.log.info("["+this.getName()+"] destroy: "+initializer);
            initializer.destroy(this);
        }
    }
    private void callInitializers(){
        for(final IInitializer initializer : this.initializers){
            Application.log.info("["+this.getName()+"] init: "+initializer);
            initializer.init(this);
        }
    }
    private void load(final Properties properties){
        this.addInitializer(properties.getProperty("initializer"));
        this.addInitializer(properties.getProperty(this.getName()+"-initializer"));
    }
    protected void onDestroy(){
    }
    protected void init(){
    }
    public void internalDestroy(){
        this.applicationListeners.onBeforeDestroyed(this);
        final IDetachListener detachListener=this.getFrameworkSettings().getDetachListener();
        if(detachListener!=null){
            detachListener.onDestroyListener();
        }
        PropertyResolver.destroy(this);
        final MarkupFactory markupFactory=this.getMarkupSettings().getMarkupFactory();
        if(markupFactory.hasMarkupCache()){
            markupFactory.getMarkupCache().shutdown();
        }
        this.onDestroy();
        this.callDestroyers();
        this.internalGetPageManager().destroy();
        this.getSessionStore().destroy();
        Application.applicationKeyToApplication.remove(this.getApplicationKey());
    }
    protected void internalInit(){
        this.settingsAccessible=true;
        final IPageSettings pageSettings=this.getPageSettings();
        pageSettings.addComponentResolver(new MarkupInheritanceResolver());
        pageSettings.addComponentResolver(new HtmlHeaderResolver());
        pageSettings.addComponentResolver(new WicketLinkTagHandler());
        pageSettings.addComponentResolver(new WicketMessageResolver());
        pageSettings.addComponentResolver(new FragmentResolver());
        pageSettings.addComponentResolver(new RelativePathPrefixHandler());
        pageSettings.addComponentResolver(new EnclosureHandler());
        pageSettings.addComponentResolver(new InlineEnclosureHandler());
        pageSettings.addComponentResolver(new WicketMessageTagHandler());
        pageSettings.addComponentResolver(new WicketContainerResolver());
        this.getResourceSettings().addResourceFactory("buttonFactory",new DefaultButtonImageResourceFactory());
        final String applicationKey=this.getApplicationKey();
        Application.applicationKeyToApplication.put(applicationKey,this);
        this.converterLocator=this.newConverterLocator();
        this.setPageManagerProvider(new DefaultPageManagerProvider(this));
        this.resourceReferenceRegistry=this.newResourceReferenceRegistry();
        this.sharedResources=this.newSharedResources(this.resourceReferenceRegistry);
        this.setRootRequestMapper((IRequestMapper)new SystemMapper(this));
        this.pageFactory=this.newPageFactory();
        this.requestCycleProvider=new DefaultRequestCycleProvider();
        this.exceptionMapperProvider=(IProvider<IExceptionMapper>)new DefaultExceptionMapperProvider();
        this.getRequestCycleListeners().add((Object)new RequestLoggerRequestCycleListener());
    }
    public IProvider<IExceptionMapper> getExceptionMapperProvider(){
        return this.exceptionMapperProvider;
    }
    public final IProvider<ISessionStore> getSessionStoreProvider(){
        return this.sessionStoreProvider;
    }
    public final void setSessionStoreProvider(final IProvider<ISessionStore> sessionStoreProvider){
        this.sessionStoreProvider=sessionStoreProvider;
    }
    protected IConverterLocator newConverterLocator(){
        return new ConverterLocator();
    }
    protected IRequestLogger newRequestLogger(){
        return new RequestLogger();
    }
    public final ICompoundRequestMapper getRootRequestMapperAsCompound(){
        IRequestMapper root=this.getRootRequestMapper();
        if(!(root instanceof ICompoundRequestMapper)){
            root=(IRequestMapper)new CompoundRequestMapper().add(root);
            this.setRootRequestMapper(root);
        }
        return (ICompoundRequestMapper)root;
    }
    public final IRequestMapper getRootRequestMapper(){
        return this.rootRequestMapper;
    }
    public final void setRootRequestMapper(final IRequestMapper rootRequestMapper){
        this.rootRequestMapper=rootRequestMapper;
    }
    public final void initApplication(){
        if(this.name==null){
            throw new IllegalStateException("setName must be called before initApplication");
        }
        this.internalInit();
        this.init();
        this.initializeComponents();
        this.applicationListeners.onAfterInitialized(this);
        this.validateInit();
    }
    protected void validateInit(){
        if(this.getPageRendererProvider()==null){
            throw new IllegalStateException("An instance of IPageRendererProvider has not yet been set on this Application. @see Application#setPageRendererProvider");
        }
        if(this.getSessionStoreProvider()==null){
            throw new IllegalStateException("An instance of ISessionStoreProvider has not yet been set on this Application. @see Application#setSessionStoreProvider");
        }
        if(this.getPageManagerProvider()==null){
            throw new IllegalStateException("An instance of IPageManagerProvider has not yet been set on this Application. @see Application#setPageManagerProvider");
        }
    }
    public final void setName(final String name){
        Args.notEmpty((CharSequence)name,"name");
        if(this.name!=null){
            throw new IllegalStateException("Application name can only be set once.");
        }
        if(Application.applicationKeyToApplication.get(name)!=null){
            throw new IllegalStateException("Application with name '"+name+"' already exists.'");
        }
        this.name=name;
        Application.applicationKeyToApplication.put(name,this);
    }
    public String getMimeType(final String fileName){
        return URLConnection.getFileNameMap().getContentTypeFor(fileName);
    }
    public void onEvent(final IEvent<?> event){
    }
    public final HeaderContributorListenerCollection getHeaderContributorListenerCollection(){
        return this.headerContributorListenerCollection;
    }
    public final List<IInitializer> getInitializers(){
        return (List<IInitializer>)Collections.unmodifiableList(this.initializers);
    }
    public final ApplicationListenerCollection getApplicationListeners(){
        return this.applicationListeners;
    }
    public final SessionListenerCollection getSessionListeners(){
        return this.sessionListeners;
    }
    public final BehaviorInstantiationListenerCollection getBehaviorInstantiationListeners(){
        return this.behaviorInstantiationListeners;
    }
    public final ComponentInstantiationListenerCollection getComponentInstantiationListeners(){
        return this.componentInstantiationListeners;
    }
    public final ComponentInitializationListenerCollection getComponentInitializationListeners(){
        return this.componentInitializationListeners;
    }
    public final ComponentOnBeforeRenderListenerCollection getComponentPreOnBeforeRenderListeners(){
        return this.componentPreOnBeforeRenderListeners;
    }
    public final ComponentOnBeforeRenderListenerCollection getComponentPostOnBeforeRenderListeners(){
        return this.componentPostOnBeforeRenderListeners;
    }
    public final ComponentOnAfterRenderListenerCollection getComponentOnAfterRenderListeners(){
        return this.componentOnAfterRenderListeners;
    }
    public RequestCycleListenerCollection getRequestCycleListeners(){
        return this.requestCycleListeners;
    }
    public final IApplicationSettings getApplicationSettings(){
        this.checkSettingsAvailable();
        if(this.applicationSettings==null){
            this.applicationSettings=new ApplicationSettings();
        }
        return this.applicationSettings;
    }
    public final void setApplicationSettings(final IApplicationSettings applicationSettings){
        this.applicationSettings=applicationSettings;
    }
    public final IDebugSettings getDebugSettings(){
        this.checkSettingsAvailable();
        if(this.debugSettings==null){
            this.debugSettings=new DebugSettings();
        }
        return this.debugSettings;
    }
    public final void setDebugSettings(final IDebugSettings debugSettings){
        this.debugSettings=debugSettings;
    }
    public final IExceptionSettings getExceptionSettings(){
        this.checkSettingsAvailable();
        if(this.exceptionSettings==null){
            this.exceptionSettings=new ExceptionSettings();
        }
        return this.exceptionSettings;
    }
    public final void setExceptionSettings(final IExceptionSettings exceptionSettings){
        this.exceptionSettings=exceptionSettings;
    }
    public final IFrameworkSettings getFrameworkSettings(){
        this.checkSettingsAvailable();
        if(this.frameworkSettings==null){
            this.frameworkSettings=new FrameworkSettings(this);
        }
        return this.frameworkSettings;
    }
    public final void setFrameworkSettings(final IFrameworkSettings frameworkSettings){
        this.frameworkSettings=frameworkSettings;
    }
    public final IPageSettings getPageSettings(){
        this.checkSettingsAvailable();
        if(this.pageSettings==null){
            this.pageSettings=new PageSettings();
        }
        return this.pageSettings;
    }
    public final void setPageSettings(final IPageSettings pageSettings){
        this.pageSettings=pageSettings;
    }
    public final IRequestCycleSettings getRequestCycleSettings(){
        this.checkSettingsAvailable();
        if(this.requestCycleSettings==null){
            this.requestCycleSettings=new RequestCycleSettings();
        }
        return this.requestCycleSettings;
    }
    public final void setRequestCycleSettings(final IRequestCycleSettings requestCycleSettings){
        this.requestCycleSettings=requestCycleSettings;
    }
    public IMarkupSettings getMarkupSettings(){
        this.checkSettingsAvailable();
        if(this.markupSettings==null){
            this.markupSettings=new MarkupSettings();
        }
        return this.markupSettings;
    }
    public final void setMarkupSettings(final IMarkupSettings markupSettings){
        this.markupSettings=markupSettings;
    }
    public final IRequestLoggerSettings getRequestLoggerSettings(){
        this.checkSettingsAvailable();
        if(this.requestLoggerSettings==null){
            this.requestLoggerSettings=new RequestLoggerSettings();
        }
        return this.requestLoggerSettings;
    }
    public final void setRequestLoggerSettings(final IRequestLoggerSettings requestLoggerSettings){
        this.requestLoggerSettings=requestLoggerSettings;
    }
    public final IResourceSettings getResourceSettings(){
        this.checkSettingsAvailable();
        if(this.resourceSettings==null){
            this.resourceSettings=new ResourceSettings(this);
        }
        return this.resourceSettings;
    }
    public final void setResourceSettings(final IResourceSettings resourceSettings){
        this.resourceSettings=resourceSettings;
    }
    public final ISecuritySettings getSecuritySettings(){
        this.checkSettingsAvailable();
        if(this.securitySettings==null){
            this.securitySettings=new SecuritySettings();
        }
        return this.securitySettings;
    }
    public final void setSecuritySettings(final ISecuritySettings securitySettings){
        this.securitySettings=securitySettings;
    }
    public final ISessionSettings getSessionSettings(){
        this.checkSettingsAvailable();
        if(this.sessionSettings==null){
            this.sessionSettings=new SessionSettings();
        }
        return this.sessionSettings;
    }
    public final void setSessionSettings(final ISessionSettings sessionSettings){
        this.sessionSettings=sessionSettings;
    }
    public final IStoreSettings getStoreSettings(){
        this.checkSettingsAvailable();
        if(this.storeSettings==null){
            this.storeSettings=new StoreSettings(this);
        }
        return this.storeSettings;
    }
    public final void setStoreSettings(final IStoreSettings storeSettings){
        this.storeSettings=storeSettings;
    }
    private void checkSettingsAvailable(){
        if(!this.settingsAccessible){
            throw new WicketRuntimeException("Use Application.init() method for configuring your application object");
        }
    }
    public final IPageManagerProvider getPageManagerProvider(){
        return this.pageManagerProvider;
    }
    public final synchronized void setPageManagerProvider(final IPageManagerProvider provider){
        this.pageManagerProvider=provider;
    }
    final IPageManager internalGetPageManager(){
        if(this.pageManager==null){
            synchronized(this){
                if(this.pageManager==null){
                    this.pageManager=(IPageManager)this.pageManagerProvider.get((Object)this.getPageManagerContext());
                }
            }
        }
        return this.pageManager;
    }
    protected IPageManagerContext getPageManagerContext(){
        return this.pageManagerContext;
    }
    public final IPageRendererProvider getPageRendererProvider(){
        return this.pageRendererProvider;
    }
    public final void setPageRendererProvider(final IPageRendererProvider pageRendererProvider){
        Args.notNull((Object)pageRendererProvider,"pageRendererProvider");
        this.pageRendererProvider=pageRendererProvider;
    }
    protected ResourceReferenceRegistry newResourceReferenceRegistry(){
        return new ResourceReferenceRegistry();
    }
    public final ResourceReferenceRegistry getResourceReferenceRegistry(){
        return this.resourceReferenceRegistry;
    }
    protected SharedResources newSharedResources(final ResourceReferenceRegistry registry){
        return new SharedResources(registry);
    }
    public SharedResources getSharedResources(){
        return this.sharedResources;
    }
    protected IPageFactory newPageFactory(){
        return new DefaultPageFactory();
    }
    public final IPageFactory getPageFactory(){
        return this.pageFactory;
    }
    public final IMapperContext getMapperContext(){
        if(this.encoderContext==null){
            this.encoderContext=this.newMapperContext();
        }
        return this.encoderContext;
    }
    protected IMapperContext newMapperContext(){
        return new DefaultMapperContext();
    }
    public Session fetchCreateAndSetSession(final RequestCycle requestCycle){
        Args.notNull((Object)requestCycle,"requestCycle");
        Session session=this.getSessionStore().lookup(requestCycle.getRequest());
        if(session==null){
            session=this.newSession(requestCycle.getRequest(),requestCycle.getResponse());
            ThreadContext.setSession(session);
            this.internalGetPageManager().newSessionCreated();
            this.sessionListeners.onCreated(session);
        }
        else{
            ThreadContext.setSession(session);
        }
        return session;
    }
    public IRequestCycleProvider getRequestCycleProvider(){
        return this.requestCycleProvider;
    }
    public void setRequestCycleProvider(final IRequestCycleProvider requestCycleProvider){
        this.requestCycleProvider=requestCycleProvider;
    }
    public final RequestCycle createRequestCycle(final Request request,final Response response){
        final RequestCycleContext context=new RequestCycleContext(request,response,this.getRootRequestMapper(),(IExceptionMapper)this.getExceptionMapperProvider().get());
        final RequestCycle requestCycle=(RequestCycle)this.getRequestCycleProvider().get((Object)context);
        requestCycle.getListeners().add((Object)this.requestCycleListeners);
        requestCycle.getListeners().add((Object)new AbstractRequestCycleListener(){
            public void onDetach(final RequestCycle requestCycle){
                if(Session.exists()){
                    Session.get().getPageManager().commitRequest();
                }
            }
            public void onEndRequest(final RequestCycle cycle){
                if(Application.exists()){
                    final IRequestLogger requestLogger=Application.get().getRequestLogger();
                    if(requestLogger!=null){
                        requestLogger.requestTime(System.currentTimeMillis()-cycle.getStartTime());
                    }
                }
            }
        });
        return requestCycle;
    }
    public void setHeaderResponseDecorator(final IHeaderResponseDecorator headerResponseDecorator){
        this.headerResponseDecorator=headerResponseDecorator;
    }
    public final IHeaderResponse decorateHeaderResponse(final IHeaderResponse response){
        if(this.headerResponseDecorator==null){
            return response;
        }
        return this.headerResponseDecorator.decorate(response);
    }
    public final boolean usesDevelopmentConfig(){
        return RuntimeConfigurationType.DEVELOPMENT.equals(this.getConfigurationType());
    }
    public final boolean usesDeploymentConfig(){
        return RuntimeConfigurationType.DEPLOYMENT.equals(this.getConfigurationType());
    }
    static{
        applicationKeyToApplication=Generics.newHashMap(1);
        log=LoggerFactory.getLogger(Application.class);
    }
    private static class DefaultExceptionMapperProvider implements IProvider<IExceptionMapper>{
        public IExceptionMapper get(){
            return (IExceptionMapper)new DefaultExceptionMapper();
        }
    }
    private static class DefaultRequestCycleProvider implements IRequestCycleProvider{
        public RequestCycle get(final RequestCycleContext context){
            return new RequestCycle(context);
        }
    }
}
