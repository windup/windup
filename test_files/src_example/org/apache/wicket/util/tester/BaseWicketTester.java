package org.apache.wicket.util.tester;

import javax.servlet.*;
import org.apache.wicket.session.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.protocol.http.mock.*;
import org.apache.wicket.util.string.*;
import java.nio.charset.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.markup.parser.*;
import java.text.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.form.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.markup.html.panel.*;
import java.lang.reflect.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.ajax.markup.html.*;
import org.apache.wicket.ajax.markup.html.form.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.form.*;
import java.io.*;
import org.apache.wicket.feedback.*;
import java.util.regex.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.ajax.*;
import junit.framework.*;
import org.slf4j.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.handler.render.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.*;
import org.apache.wicket.page.*;
import org.apache.wicket.mock.*;
import java.util.*;
import org.apache.wicket.protocol.http.servlet.*;
import org.apache.wicket.protocol.http.*;
import javax.servlet.http.*;
import org.apache.wicket.request.http.*;

public class BaseWicketTester{
    private static final Logger log;
    private final ServletContext servletContext;
    private MockHttpSession httpSession;
    private final WebApplication application;
    private boolean followRedirects;
    private int redirectCount;
    private MockHttpServletRequest lastRequest;
    private MockHttpServletResponse lastResponse;
    private final List<MockHttpServletRequest> previousRequests;
    private final List<MockHttpServletResponse> previousResponses;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Session session;
    private RequestCycle requestCycle;
    private Page lastRenderedPage;
    private boolean exposeExceptions;
    private boolean useRequestUrlAsBase;
    private IRequestHandler forcedHandler;
    private ComponentInPage componentInPage;
    private Map<String,String> preHeader;
    private Method newWebRequestMethod;
    public BaseWicketTester(){
        this(new MockApplication());
    }
    public BaseWicketTester(final Class<C> homePage){
        this(new MockApplication(){
            public Class<? extends Page> getHomePage(){
                return homePage;
            }
        });
    }
    public BaseWicketTester(final WebApplication application){
        this(application,(ServletContext)null);
    }
    public BaseWicketTester(final WebApplication application,final String servletContextBasePath){
        this(application,new MockServletContext(application,servletContextBasePath));
    }
    public BaseWicketTester(final WebApplication application,final ServletContext servletCtx){
        super();
        this.followRedirects=true;
        this.previousRequests=(List<MockHttpServletRequest>)Generics.newArrayList();
        this.previousResponses=(List<MockHttpServletResponse>)Generics.newArrayList();
        this.exposeExceptions=true;
        this.useRequestUrlAsBase=true;
        this.newWebRequestMethod=null;
        this.servletContext=((servletCtx!=null)?servletCtx:new MockServletContext(application,null));
        final FilterConfig filterConfig=new TestFilterConfig();
        final WicketFilter filter=new WicketFilter(){
            public FilterConfig getFilterConfig(){
                return filterConfig;
            }
        };
        application.setWicketFilter(filter);
        this.httpSession=new MockHttpSession(this.servletContext);
        ThreadContext.detach();
        (this.application=application).setName("WicketTesterApplication-"+UUID.randomUUID());
        ThreadContext.setApplication(application);
        application.setServletContext(this.servletContext);
        application.initApplication();
        application.getResourceSettings().setResourcePollFrequency(this.getResourcePollFrequency());
        application.setPageRendererProvider(new LastPageRecordingPageRendererProvider(application.getPageRendererProvider()));
        application.setRequestCycleProvider(new TestRequestCycleProvider(application.getRequestCycleProvider()));
        final IPageManagerProvider pageManagerProvider=this.newTestPageManagerProvider();
        if(pageManagerProvider!=null){
            application.setPageManagerProvider(pageManagerProvider);
        }
        application.getSessionStore().registerUnboundListener(new ISessionStore.UnboundListener(){
            public void sessionUnbound(final String sessionId){
                BaseWicketTester.this.newSession();
            }
        });
        this.setupNextRequestCycle();
    }
    protected Duration getResourcePollFrequency(){
        return null;
    }
    protected IPageManagerProvider newTestPageManagerProvider(){
        return new TestPageManagerProvider();
    }
    public Page getLastRenderedPage(){
        return this.lastRenderedPage;
    }
    private void setupNextRequestCycle(){
        (this.request=new MockHttpServletRequest(this.application,this.httpSession,this.servletContext)).setURL(this.request.getContextPath()+this.request.getServletPath()+"/");
        final boolean assignBaseLocation=this.lastRequest!=null&&this.lastRequest.getHeader("Wicket-Ajax")==null;
        if(assignBaseLocation){
            this.request.setScheme(this.lastRequest.getScheme());
            this.request.setSecure(this.lastRequest.isSecure());
            this.request.setServerName(this.lastRequest.getServerName());
            this.request.setServerPort(this.lastRequest.getServerPort());
        }
        this.transferRequestCookies();
        this.response=new MockHttpServletResponse(this.request);
        if(this.lastResponse!=null&&this.lastResponse.isRedirect()){
            final List<Cookie> lastResponseCookies=this.lastResponse.getCookies();
            for(final Cookie cookie : lastResponseCookies){
                if(cookie.getMaxAge()!=0){
                    this.response.addCookie(cookie);
                }
            }
        }
        final ServletWebRequest servletWebRequest=this.newServletWebRequest();
        (this.requestCycle=this.application.createRequestCycle((Request)servletWebRequest,this.newServletWebResponse(servletWebRequest))).setCleanupFeedbackMessagesOnDetach(false);
        ThreadContext.setRequestCycle(this.requestCycle);
        if(this.session==null){
            this.newSession();
        }
    }
    private void transferRequestCookies(){
        final List<Cookie> lastRequestCookies=(List<Cookie>)new ArrayList();
        if(this.lastRequest!=null&&this.lastRequest.getCookies()!=null){
            for(final Cookie lastRequestCookie : this.lastRequest.getCookies()){
                lastRequestCookies.add(lastRequestCookie);
            }
        }
        if(this.lastResponse!=null){
            final List<Cookie> cookies=this.lastResponse.getCookies();
            if(cookies!=null){
                for(final Cookie cookie : cookies){
                    if(cookie.getMaxAge()==0){
                        final Iterator<Cookie> cookieIterator=(Iterator<Cookie>)lastRequestCookies.iterator();
                        while(cookieIterator.hasNext()){
                            final Cookie lastRequestCookie2=(Cookie)cookieIterator.next();
                            if(Cookies.isEqual(lastRequestCookie2,cookie)){
                                cookieIterator.remove();
                            }
                        }
                    }
                    else{
                        boolean newlyCreated=true;
                        for(final Cookie oldCookie : lastRequestCookies){
                            if(Cookies.isEqual(cookie,oldCookie)){
                                newlyCreated=false;
                                break;
                            }
                        }
                        if(!newlyCreated){
                            continue;
                        }
                        lastRequestCookies.add(cookie);
                    }
                }
            }
        }
        this.request.addCookies((Iterable<Cookie>)lastRequestCookies);
    }
    protected Response newServletWebResponse(final ServletWebRequest servletWebRequest){
        return (Response)new WicketTesterServletWebResponse(servletWebRequest,this.response);
    }
    private ServletWebRequest newServletWebRequest(){
        if(this.newWebRequestMethod==null){
            try{
                (this.newWebRequestMethod=WebApplication.class.getDeclaredMethod("newWebRequest",new Class[] { HttpServletRequest.class,String.class })).setAccessible(true);
            }
            catch(Exception e){
                throw new RuntimeException((Throwable)e);
            }
        }
        ServletWebRequest webRequest;
        try{
            webRequest=(ServletWebRequest)this.newWebRequestMethod.invoke(this.application,new Object[] { this.request,this.request.getFilterPrefix() });
        }
        catch(Exception x){
            throw new RuntimeException((Throwable)x);
        }
        return webRequest;
    }
    private void newSession(){
        ThreadContext.setSession(null);
        this.session=Session.get();
    }
    public MockHttpServletRequest getRequest(){
        return this.request;
    }
    public void setRequest(final MockHttpServletRequest request){
        this.request=request;
        this.applyRequest();
    }
    public Session getSession(){
        return this.session;
    }
    public MockHttpSession getHttpSession(){
        return this.httpSession;
    }
    public WebApplication getApplication(){
        return this.application;
    }
    public ServletContext getServletContext(){
        return this.servletContext;
    }
    public void destroy(){
        this.application.internalDestroy();
        ThreadContext.detach();
    }
    public boolean processRequest(){
        return this.processRequest(null,null);
    }
    public boolean processRequest(final MockHttpServletRequest request){
        return this.processRequest(request,null);
    }
    public boolean processRequest(final MockHttpServletRequest request,final IRequestHandler forcedRequestHandler){
        return this.processRequest(request,forcedRequestHandler,false);
    }
    public boolean processRequest(final IRequestHandler forcedRequestHandler){
        return this.processRequest(null,forcedRequestHandler,false);
    }
    protected boolean processRequest(final MockHttpServletRequest forcedRequest,final IRequestHandler forcedRequestHandler,final boolean redirect){
        if(forcedRequest!=null){
            this.request=forcedRequest;
        }
        this.forcedHandler=forcedRequestHandler;
        if(!redirect&&this.getRequest().getHeader("Wicket-Ajax")==null){
            this.lastRenderedPage=null;
        }
        if(this.request!=null&&this.preHeader!=null){
            for(final Map.Entry<String,String> entry : this.preHeader.entrySet()){
                if(!Strings.isEmpty((CharSequence)entry.getKey())){
                    this.request.setHeader((String)entry.getKey(),(String)entry.getValue());
                }
            }
            this.preHeader=null;
        }
        try{
            if(!redirect){
                this.session.cleanupFeedbackMessages();
            }
            this.transferRequestCookies();
            this.applyRequest();
            this.requestCycle.scheduleRequestHandlerAfterCurrent(null);
            if(!this.requestCycle.processRequestAndDetach()){
                return false;
            }
            this.recordRequestResponse();
            this.setupNextRequestCycle();
            if(this.followRedirects&&this.lastResponse.isRedirect()){
                if(this.redirectCount++>=100){
                    Assert.fail("Possible infinite redirect detected. Bailing out.");
                }
                final Url newUrl=Url.parse(this.lastResponse.getRedirectLocation(),Charset.forName(this.request.getCharacterEncoding()));
                if(this.isExternalRedirect(this.lastRequest.getUrl(),newUrl)){
                    return true;
                }
                if(newUrl.isAbsolute()){
                    this.request.setUrl(newUrl);
                    final String protocol=newUrl.getProtocol();
                    if(protocol!=null){
                        this.request.setScheme(protocol);
                    }
                    this.request.setSecure("https".equals(protocol));
                    if(newUrl.getHost()!=null){
                        this.request.setServerName(newUrl.getHost());
                    }
                    if(newUrl.getPort()!=null){
                        this.request.setServerPort(newUrl.getPort());
                    }
                }
                else{
                    final Url mergedURL=new Url(this.lastRequest.getUrl().getSegments(),newUrl.getQueryParameters());
                    mergedURL.concatSegments(newUrl.getSegments());
                    this.request.setUrl(mergedURL);
                }
                this.processRequest(null,null,true);
                --this.redirectCount;
            }
            return true;
        }
        finally{
            this.redirectCount=0;
        }
    }
    private boolean isExternalRedirect(final Url requestUrl,final Url newUrl){
        final String originalHost=requestUrl.getHost();
        final String redirectHost=newUrl.getHost();
        final Integer originalPort=requestUrl.getPort();
        final Integer newPort=newUrl.getPort();
        return !originalHost.equals(redirectHost)&&redirectHost!=null&&(!originalPort.equals(newPort)||!redirectHost.equals(originalHost));
    }
    public final void addRequestHeader(final String key,final String value){
        Args.notEmpty((CharSequence)key,"key");
        if(this.preHeader==null){
            this.preHeader=(Map<String,String>)Generics.newHashMap();
        }
        this.preHeader.put(key,value);
    }
    private void recordRequestResponse(){
        this.lastRequest=this.request;
        this.lastResponse=this.response;
        this.previousRequests.add(this.request);
        this.previousResponses.add(this.response);
    }
    public Page startPage(final IPageProvider pageProvider){
        this.componentInPage=null;
        this.request.setURL(this.request.getContextPath()+this.request.getServletPath()+"/");
        final IRequestHandler handler=(IRequestHandler)new RenderPageRequestHandler(pageProvider);
        this.processRequest(this.request,handler);
        return this.getLastRenderedPage();
    }
    public Page startPage(final Page page){
        return this.startPage(new PageProvider(page));
    }
    public ResourceReference startResource(final IResource resource){
        return this.startResourceReference(new ResourceReference("testResourceReference"){
            private static final long serialVersionUID=1L;
            public IResource getResource(){
                return resource;
            }
        });
    }
    public ResourceReference startResourceReference(final ResourceReference reference){
        return this.startResourceReference(reference,null);
    }
    public ResourceReference startResourceReference(final ResourceReference reference,final PageParameters pageParameters){
        this.request.setURL(this.request.getContextPath()+this.request.getServletPath()+"/");
        final IRequestHandler handler=(IRequestHandler)new ResourceReferenceRequestHandler(reference,pageParameters);
        this.processRequest(this.request,handler);
        return reference;
    }
    public MockHttpServletResponse getLastResponse(){
        return this.lastResponse;
    }
    public String getLastResponseAsString(){
        String response=this.lastResponse.getDocument();
        if(this.componentInPage==null){
            return response;
        }
        final int end=response.lastIndexOf("</body>");
        if(end>-1){
            final int start=response.indexOf("<body>")+"<body>".length();
            response=response.substring(start,end);
        }
        return response;
    }
    public String getWicketAjaxBaseUrlEncodedInLastResponse() throws IOException,ResourceStreamNotFoundException,ParseException{
        final XmlPullParser parser=new XmlPullParser();
        parser.parse((CharSequence)this.getLastResponseAsString());
        XmlTag tag;
        while((tag=parser.nextTag())!=null){
            if(tag.isOpen()&&tag.getName().equals("script")&&"wicket-ajax-base-url".equals(tag.getAttribute("id"))){
                parser.next();
                return parser.getString().toString().split("\\\"")[1];
            }
        }
        Assert.fail("Last response has no AJAX base URL set by AbstractDefaultAjaxBehavior.");
        return null;
    }
    public List<MockHttpServletRequest> getPreviousRequests(){
        return (List<MockHttpServletRequest>)Collections.unmodifiableList(this.previousRequests);
    }
    public List<MockHttpServletResponse> getPreviousResponses(){
        return (List<MockHttpServletResponse>)Collections.unmodifiableList(this.previousResponses);
    }
    public void setFollowRedirects(final boolean followRedirects){
        this.followRedirects=followRedirects;
    }
    public boolean isFollowRedirects(){
        return this.followRedirects;
    }
    public Url urlFor(final IRequestHandler handler){
        final Url url=this.application.getRootRequestMapper().mapHandler(handler);
        return this.transform(url);
    }
    public String urlFor(final Link<?> link){
        Args.notNull((Object)link,"link");
        final Url url=Url.parse(link.urlFor(ILinkListener.INTERFACE,new PageParameters()).toString());
        return this.transform(url).toString();
    }
    @Deprecated
    public final Page startPage(final ITestPageSource testPageSource){
        Args.notNull((Object)testPageSource,"testPageResource");
        return this.startPage(testPageSource.getTestPage());
    }
    public void executeListener(final Component component,final RequestListenerInterface listener){
        Args.notNull((Object)component,"component");
        final IRequestHandler handler=(IRequestHandler)new ListenerInterfaceRequestHandler(new PageAndComponentProvider(component.getPage(),component),listener);
        final Url url=this.urlFor(handler);
        final MockHttpServletRequest request=new MockHttpServletRequest(this.application,this.httpSession,this.servletContext);
        request.setUrl(url);
        this.processRequest(request,null);
    }
    public void invokeListener(final Component component,final RequestListenerInterface listener){
        Args.notNull((Object)component,"component");
        final IRequestHandler handler=(IRequestHandler)new ListenerInterfaceRequestHandler(new PageAndComponentProvider(component.getPage(),component),listener);
        this.processRequest(handler);
    }
    public void executeListener(final Component component){
        Args.notNull((Object)component,"component");
        for(final RequestListenerInterface iface : RequestListenerInterface.getRegisteredInterfaces()){
            if(iface.getListenerInterfaceClass().isAssignableFrom(component.getClass())){
                this.executeListener(component,iface);
            }
        }
    }
    public void executeBehavior(final AbstractAjaxBehavior behavior){
        Args.notNull((Object)behavior,"behavior");
        final Url url=Url.parse(behavior.getCallbackUrl().toString(),Charset.forName(this.request.getCharacterEncoding()));
        this.transform(url);
        this.request.setUrl(url);
        this.request.addHeader("Wicket-Ajax-BaseURL",url.toString());
        this.request.addHeader("Wicket-Ajax","true");
        if(behavior instanceof AjaxFormSubmitBehavior){
            final AjaxFormSubmitBehavior formSubmitBehavior=(AjaxFormSubmitBehavior)behavior;
            final Form<?> form=formSubmitBehavior.getForm();
            this.getRequest().setUseMultiPartContentType(form.isMultiPart());
            this.serializeFormToRequest(form);
        }
        this.processRequest();
    }
    public Url urlFor(final AjaxLink<?> link){
        final AbstractAjaxBehavior behavior=WicketTesterHelper.findAjaxEventBehavior(link,"onclick");
        final Url url=Url.parse(behavior.getCallbackUrl().toString(),Charset.forName(this.request.getCharacterEncoding()));
        return this.transform(url);
    }
    public void executeAjaxUrl(final Url url){
        Args.notNull((Object)url,"url");
        this.transform(url);
        this.request.setUrl(url);
        this.request.addHeader("Wicket-Ajax-BaseURL",url.toString());
        this.request.addHeader("Wicket-Ajax","true");
        this.processRequest();
    }
    public final <C extends Page> C startPage(final Class<C> pageClass){
        return this.startPage(pageClass,null);
    }
    public final <C extends Page> C startPage(final Class<C> pageClass,final PageParameters parameters){
        Args.notNull((Object)pageClass,"pageClass");
        this.componentInPage=null;
        this.request.setUrl(this.application.getRootRequestMapper().mapHandler((IRequestHandler)new BookmarkablePageRequestHandler(new PageProvider(pageClass,parameters))));
        this.processRequest();
        return (C)this.getLastRenderedPage();
    }
    public FormTester newFormTester(final String path){
        return this.newFormTester(path,true);
    }
    public FormTester newFormTester(final String path,final boolean fillBlankString){
        return new FormTester(path,(Form<?>)this.getComponentFromLastRenderedPage(path),this,fillBlankString);
    }
    @Deprecated
    public final Panel startPanel(final ITestPanelSource testPanelSource){
        Args.notNull((Object)testPanelSource,"testPanelSource");
        return this.startComponentInPage(testPanelSource.getTestPanel("panel"),null);
    }
    @Deprecated
    public final <C extends Panel> C startPanel(final Class<C> panelClass){
        Args.notNull((Object)panelClass,"panelClass");
        return this.startComponentInPage(panelClass,(IMarkupFragment)null);
    }
    public final <C extends Component> C startComponentInPage(final Class<C> componentClass){
        return this.startComponentInPage(componentClass,(IMarkupFragment)null);
    }
    public final <C extends Component> C startComponentInPage(final Class<C> componentClass,final IMarkupFragment pageMarkup){
        Args.notNull((Object)componentClass,"componentClass");
        C comp=null;
        try{
            final Constructor<C> c=(Constructor<C>)componentClass.getConstructor(new Class[] { String.class });
            comp=(C)c.newInstance(new Object[] { "testObject" });
            this.componentInPage=new ComponentInPage();
            this.componentInPage.component=comp;
            this.componentInPage.isInstantiated=true;
        }
        catch(Exception e){
            BaseWicketTester.log.error(e.getMessage(),e);
            Assert.fail(String.format("Cannot instantiate component with type '%s' because of '%s'",new Object[] { componentClass.getName(),e.getMessage() }));
        }
        return this.startComponentInPage(comp,pageMarkup);
    }
    public final <C extends Component> C startComponentInPage(final C component){
        return this.startComponentInPage(component,null);
    }
    public final <C extends Component> C startComponentInPage(final C component,IMarkupFragment pageMarkup){
        Args.notNull((Object)component,"component");
        final Page page=this.createPage();
        if(page==null){
            Assert.fail("The automatically created page should not be null.");
        }
        if(pageMarkup==null){
            final String markup=this.createPageMarkup(component.getId());
            if(markup==null){
                Assert.fail("The markup for the automatically created page should not be null.");
            }
            try{
                final ContainerInfo containerInfo=new ContainerInfo(page);
                final MarkupResourceStream markupResourceStream=new MarkupResourceStream((IResourceStream)new StringResourceStream((CharSequence)markup),containerInfo,(Class<?>)page.getClass());
                final MarkupParser markupParser=this.getApplication().getMarkupSettings().getMarkupFactory().newMarkupParser(markupResourceStream);
                pageMarkup=markupParser.parse();
            }
            catch(Exception e){
                Assert.fail("Error while parsing the markup for the autogenerated page: "+e.getMessage());
            }
        }
        page.setMarkup(pageMarkup);
        page.add(component);
        final ComponentInPage oldComponentInPage=this.componentInPage;
        this.startPage(page);
        if(oldComponentInPage!=null){
            this.componentInPage=oldComponentInPage;
        }
        else{
            this.componentInPage=new ComponentInPage();
            this.componentInPage.component=component;
        }
        return component;
    }
    protected String createPageMarkup(final String componentId){
        return "<html><head></head><body><span wicket:id='"+componentId+"'></span></body></html>";
    }
    protected Page createPage(){
        return new StartComponentInPage();
    }
    public Component startComponent(final Component component){
        try{
            component.internalInitialize();
            if(component instanceof FormComponent){
                ((FormComponent)component).processInput();
            }
            component.beforeRender();
        }
        finally{
            this.getRequestCycle().detach();
            component.detach();
        }
        return component;
    }
    public Component getComponentFromLastRenderedPage(String path,final boolean wantVisibleInHierarchy){
        if(this.componentInPage!=null&&this.componentInPage.isInstantiated){
            final String componentIdPageId=this.componentInPage.component.getId()+':';
            if(!path.startsWith(componentIdPageId)){
                path=componentIdPageId+path;
            }
        }
        final Component component=this.getLastRenderedPage().get(path);
        if(component==null){
            Assert.fail("path: '"+path+"' does not exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
            return null;
        }
        if(!wantVisibleInHierarchy||component.isVisibleInHierarchy()){
            return component;
        }
        return null;
    }
    public Component getComponentFromLastRenderedPage(final String path){
        return this.getComponentFromLastRenderedPage(path,true);
    }
    public Result hasLabel(final String path,final String expectedLabelText){
        final Label label=(Label)this.getComponentFromLastRenderedPage(path);
        return this.isEqual(expectedLabelText,label.getDefaultModelObjectAsString());
    }
    public <C extends Component> Result isComponent(final String path,final Class<C> expectedComponentClass){
        final Component component=this.getComponentFromLastRenderedPage(path);
        if(component==null){
            return Result.fail("Component not found: "+path);
        }
        return this.isTrue("component '"+Classes.simpleName(component.getClass())+"' is not type:"+Classes.simpleName((Class)expectedComponentClass),expectedComponentClass.isAssignableFrom(component.getClass()));
    }
    public Result isVisible(final String path){
        final Component component=this.getComponentFromLastRenderedPage(path,false);
        Result result;
        if(component==null){
            result=Result.fail("path: '"+path+"' does no exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
        }
        else{
            result=this.isTrue("component '"+path+"' is not visible",component.isVisibleInHierarchy());
        }
        return result;
    }
    public Result isInvisible(final String path){
        final Component component=this.getComponentFromLastRenderedPage(path,false);
        Result result;
        if(component==null){
            result=Result.fail("path: '"+path+"' does no exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
        }
        else{
            result=this.isFalse("component '"+path+"' is visible",component.isVisibleInHierarchy());
        }
        return result;
    }
    public Result isEnabled(final String path){
        final Component component=this.getComponentFromLastRenderedPage(path);
        if(component==null){
            Assert.fail("path: '"+path+"' does no exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
        }
        return this.isTrue("component '"+path+"' is disabled",component.isEnabledInHierarchy());
    }
    public Result isDisabled(final String path){
        final Component component=this.getComponentFromLastRenderedPage(path);
        if(component==null){
            Assert.fail("path: '"+path+"' does no exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
        }
        return this.isFalse("component '"+path+"' is enabled",component.isEnabledInHierarchy());
    }
    public Result isRequired(final String path){
        final Component component=this.getComponentFromLastRenderedPage(path);
        if(component==null){
            Assert.fail("path: '"+path+"' does no exist for page: "+Classes.simpleName(this.getLastRenderedPage().getClass()));
        }
        else if(!(component instanceof FormComponent)){
            Assert.fail("path: '"+path+"' is not a form component");
        }
        return this.isRequired((FormComponent<?>)component);
    }
    public Result isRequired(final FormComponent<?> component){
        return this.isTrue("component '"+component+"' is not required",component.isRequired());
    }
    public Result ifContains(final String pattern){
        return this.isTrue("pattern '"+pattern+"' not found in:\n"+this.getLastResponseAsString(),this.getLastResponseAsString().matches("(?s).*"+pattern+".*"));
    }
    public Result ifContainsNot(final String pattern){
        return this.isFalse("pattern '"+pattern+"' found",this.getLastResponseAsString().matches("(?s).*"+pattern+".*"));
    }
    public void assertListView(final String path,final List<?> expectedList){
        final ListView<?> listView=(ListView<?>)this.getComponentFromLastRenderedPage(path);
        WicketTesterHelper.assertEquals((Collection<?>)expectedList,(Collection<?>)listView.getList());
    }
    public void clickLink(final String path){
        this.clickLink(path,true);
    }
    public void clickLink(final String path,final boolean isAjax){
        final Component linkComponent=this.getComponentFromLastRenderedPage(path);
        this.checkUsability(linkComponent,true);
        if(linkComponent instanceof AjaxLink){
            if(!isAjax){
                Assert.fail("Link "+path+"is an AjaxLink and will "+"not be invoked when AJAX (javascript) is disabled.");
            }
            this.executeBehavior(WicketTesterHelper.findAjaxEventBehavior(linkComponent,"onclick"));
        }
        else if(linkComponent instanceof AjaxFallbackLink&&isAjax){
            this.executeBehavior(WicketTesterHelper.findAjaxEventBehavior(linkComponent,"onclick"));
        }
        else if(linkComponent instanceof AjaxSubmitLink){
            if(!isAjax){
                Assert.fail("Link "+path+" is an AjaxSubmitLink and "+"will not be invoked when AJAX (javascript) is disabled.");
            }
            final AjaxSubmitLink link=(AjaxSubmitLink)linkComponent;
            final String pageRelativePath=link.getInputName();
            this.request.getPostParameters().setParameterValue(pageRelativePath,"x");
            this.submitAjaxFormSubmitBehavior(link,(AjaxFormSubmitBehavior)WicketTesterHelper.findAjaxEventBehavior(link,"onclick"));
        }
        else if(linkComponent instanceof SubmitLink){
            final SubmitLink submitLink=(SubmitLink)linkComponent;
            final String pageRelativePath=submitLink.getInputName();
            this.request.getPostParameters().setParameterValue(pageRelativePath,"x");
            this.submitForm(submitLink.getForm().getPageRelativePath());
        }
        else if(linkComponent instanceof AbstractLink){
            final AbstractLink link2=(AbstractLink)linkComponent;
            if(link2 instanceof BookmarkablePageLink){
                final BookmarkablePageLink<?> bookmarkablePageLink=(BookmarkablePageLink<?>)link2;
                try{
                    final Method getParametersMethod=BookmarkablePageLink.class.getDeclaredMethod("getPageParameters",null);
                    getParametersMethod.setAccessible(true);
                    final PageParameters parameters=(PageParameters)getParametersMethod.invoke(bookmarkablePageLink,null);
                    this.startPage(bookmarkablePageLink.getPageClass(),parameters);
                }
                catch(Exception e){
                    throw new WicketRuntimeException("Internal error in WicketTester. Please report this in Wicket's Issue Tracker.",e);
                }
            }
            else{
                if(link2 instanceof ResourceLink){
                    try{
                        final Method getURL=ResourceLink.class.getDeclaredMethod("getURL",new Class[0]);
                        getURL.setAccessible(true);
                        final CharSequence url=(CharSequence)getURL.invoke(link2,new Object[0]);
                        this.executeUrl(url.toString());
                        return;
                    }
                    catch(Exception x){
                        throw new RuntimeException("An error occurred while clicking on a ResourceLink",(Throwable)x);
                    }
                }
                this.executeListener(link2,ILinkListener.INTERFACE);
            }
        }
        else{
            Assert.fail("Link "+path+" is not a Link, AjaxLink, AjaxFallbackLink or AjaxSubmitLink");
        }
    }
    public void submitForm(final Form<?> form){
        this.submitForm(form.getPageRelativePath());
    }
    public void submitForm(final String path){
        final Form<?> form=(Form<?>)this.getComponentFromLastRenderedPage(path);
        final Url url=Url.parse(form.getRootForm().urlFor(IFormSubmitListener.INTERFACE,new PageParameters()).toString(),Charset.forName(this.request.getCharacterEncoding()));
        this.transform(url);
        this.request.setUrl(url);
        this.processRequest();
    }
    private Url transform(final Url url){
        while(url.getSegments().size()>0&&(((String)url.getSegments().get(0)).equals("..")||((String)url.getSegments().get(0)).equals("."))){
            url.getSegments().remove(0);
        }
        return url;
    }
    public <C extends Page> Result isRenderedPage(final Class<C> expectedRenderedPageClass){
        Args.notNull((Object)expectedRenderedPageClass,"expectedRenderedPageClass");
        final Page page=this.getLastRenderedPage();
        if(page==null){
            return Result.fail("page was null");
        }
        if(!expectedRenderedPageClass.isAssignableFrom(page.getClass())){
            return Result.fail(String.format("classes not the same, expected '%s', current '%s'",new Object[] { expectedRenderedPageClass,page.getClass() }));
        }
        return Result.pass();
    }
    public void assertResultPage(final Class<?> pageClass,final String filename) throws Exception{
        final String document=this.getLastResponseAsString();
        DiffUtil.validatePage(document,pageClass,filename,true);
    }
    public Result isResultPage(final String expectedDocument) throws Exception{
        final String document=this.getLastResponseAsString();
        return this.isTrue("expected rendered page equals",document.equals(expectedDocument));
    }
    public Result hasNoErrorMessage(){
        final List<Serializable> messages=this.getMessages(400);
        return this.isTrue("expect no error message, but contains\n"+WicketTesterHelper.asLined((Collection<?>)messages),messages.isEmpty());
    }
    public Result hasNoInfoMessage(){
        final List<Serializable> messages=this.getMessages(200);
        return this.isTrue("expect no info message, but contains\n"+WicketTesterHelper.asLined((Collection<?>)messages),messages.isEmpty());
    }
    public List<Serializable> getMessages(final int level){
        final FeedbackMessages feedbackMessages=Session.get().getFeedbackMessages();
        final List<FeedbackMessage> allMessages=feedbackMessages.messages(new IFeedbackMessageFilter(){
            private static final long serialVersionUID=1L;
            public boolean accept(final FeedbackMessage message){
                return message.getLevel()==level;
            }
        });
        final List<Serializable> actualMessages=(List<Serializable>)Generics.newArrayList();
        for(final FeedbackMessage message : allMessages){
            actualMessages.add(message.getMessage());
        }
        return actualMessages;
    }
    public void dumpPage(){
        BaseWicketTester.log.info(this.getLastResponseAsString());
    }
    public void debugComponentTrees(){
        this.debugComponentTrees("");
    }
    public void debugComponentTrees(final String filter){
        BaseWicketTester.log.info("debugging ----------------------------------------------");
        for(final WicketTesterHelper.ComponentData obj : WicketTesterHelper.getComponentData(this.getLastRenderedPage())){
            if(obj.path.matches(".*"+filter+".*")){
                BaseWicketTester.log.info("path\t"+obj.path+" \t"+obj.type+" \t["+obj.value+"]");
            }
        }
    }
    public Result isComponentOnAjaxResponse(final Component component){
        String failMessage="A component which is null could not have been added to the AJAX response";
        this.notNull(failMessage,component);
        if(!component.isVisible()){
            failMessage="A component which is invisible and doesn't render a placeholder tag will not be rendered at all and thus won't be accessible for subsequent AJAX interaction";
            final Result result=this.isTrue(failMessage,component.getOutputMarkupPlaceholderTag());
            if(result.wasFailed()){
                return result;
            }
        }
        final String ajaxResponse=this.getLastResponseAsString();
        failMessage="The Previous response was not an AJAX response. You need to execute an AJAX event, using clickLink, before using this assert";
        final boolean isAjaxResponse=Pattern.compile("^<\\?xml version=\"1.0\" encoding=\".*?\"\\?><ajax-response>").matcher((CharSequence)ajaxResponse).find();
        Result result=this.isTrue(failMessage,isAjaxResponse);
        if(result.wasFailed()){
            return result;
        }
        final String markupId=component.getMarkupId();
        failMessage="The component doesn't have a markup id, which means that it can't have been added to the AJAX response";
        result=this.isTrue(failMessage,!Strings.isEmpty((CharSequence)markupId));
        if(result.wasFailed()){
            return result;
        }
        final boolean isComponentInAjaxResponse=ajaxResponse.matches("(?s).*<component id=\""+markupId+"\"[^>]*?>.*");
        failMessage="Component wasn't found in the AJAX response";
        return this.isTrue(failMessage,isComponentInAjaxResponse);
    }
    public void executeAjaxEvent(final String componentPath,final String event){
        final Component component=this.getComponentFromLastRenderedPage(componentPath);
        this.executeAjaxEvent(component,event);
    }
    public void executeAllTimerBehaviors(final MarkupContainer page){
        this.internalExecuteAllTimerBehaviors(page);
        page.visitChildren((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                BaseWicketTester.this.internalExecuteAllTimerBehaviors(component);
            }
        });
    }
    private void internalExecuteAllTimerBehaviors(final Component component){
        final List<AbstractAjaxTimerBehavior> behaviors=component.getBehaviors((Class<AbstractAjaxTimerBehavior>)AbstractAjaxTimerBehavior.class);
        for(final AbstractAjaxTimerBehavior timer : behaviors){
            this.checkUsability(component,true);
            if(!timer.isStopped()){
                if(BaseWicketTester.log.isDebugEnabled()){
                    BaseWicketTester.log.debug("Triggering AjaxSelfUpdatingTimerBehavior: {}",component.getClassRelativePath());
                }
                this.executeBehavior(timer);
            }
        }
    }
    public void executeAjaxEvent(final Component component,final String event){
        Args.notNull((Object)component,"component");
        Args.notNull((Object)event,"event");
        this.checkUsability(component,true);
        final AjaxEventBehavior ajaxEventBehavior=WicketTesterHelper.findAjaxEventBehavior(component,event);
        this.executeBehavior(ajaxEventBehavior);
    }
    public TagTester getTagByWicketId(final String wicketId){
        return TagTester.createTagByAttribute(this.getLastResponseAsString(),"wicket:id",wicketId);
    }
    public List<TagTester> getTagsByWicketId(final String wicketId){
        return TagTester.createTagsByAttribute(this.getLastResponseAsString(),"wicket:id",wicketId,false);
    }
    public TagTester getTagById(final String id){
        return TagTester.createTagByAttribute(this.getLastResponseAsString(),"id",id);
    }
    private void submitAjaxFormSubmitBehavior(final Component component,final AjaxFormSubmitBehavior behavior){
        final Form<?> form=behavior.getForm();
        Assert.assertNotNull("No form attached to the submitlink.",form);
        this.checkUsability(form,true);
        this.serializeFormToRequest(form);
        this.executeBehavior(behavior);
    }
    private void serializeFormToRequest(final Form<?> form){
        final MockRequestParameters postParameters=this.request.getPostParameters();
        final Set<String> currentParameterNamesSet=postParameters.getParameterNames();
        form.visitFormComponents((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                final String inputName=formComponent.getInputName();
                if(!currentParameterNamesSet.contains(inputName)){
                    final String[] arr$;
                    final String[] values=arr$=FormTester.getInputValue(formComponent);
                    for(final String value : arr$){
                        postParameters.addParameterValue(inputName,value);
                    }
                }
            }
        });
    }
    public String getContentTypeFromResponseHeader(){
        final String contentType=this.getLastResponse().getContentType();
        Assert.assertNotNull("No Content-Type header found",contentType);
        return contentType;
    }
    public int getContentLengthFromResponseHeader(){
        final String contentLength=this.getLastResponse().getHeader("Content-Length");
        Assert.assertNotNull("No Content-Length header found",contentLength);
        return Integer.parseInt(contentLength);
    }
    public String getLastModifiedFromResponseHeader(){
        return this.getLastResponse().getHeader("Last-Modified");
    }
    public String getContentDispositionFromResponseHeader(){
        return this.getLastResponse().getHeader("Content-Disposition");
    }
    public void applyRequest(){
        final Request req=(Request)this.newServletWebRequest();
        this.requestCycle.setRequest(req);
        if(this.useRequestUrlAsBase){
            this.requestCycle.getUrlRenderer().setBaseUrl(req.getUrl());
        }
    }
    private Result isTrue(final String message,final boolean condition){
        if(condition){
            return Result.pass();
        }
        return Result.fail(message);
    }
    private Result isFalse(final String message,final boolean condition){
        if(!condition){
            return Result.pass();
        }
        return Result.fail(message);
    }
    protected final Result isEqual(final Object expected,final Object actual){
        if(expected==null&&actual==null){
            return Result.pass();
        }
        if(expected!=null&&expected.equals(actual)){
            return Result.pass();
        }
        final String message="expected:<"+expected+"> but was:<"+actual+">";
        return Result.fail(message);
    }
    private void notNull(final String message,final Object object){
        if(object==null){
            Assert.fail(message);
        }
    }
    private Result isNull(final String message,final Object object){
        if(object!=null){
            return Result.fail(message);
        }
        return Result.pass();
    }
    protected Result checkUsability(final Component component,final boolean throwException){
        Result res=Result.pass();
        if(!component.isVisibleInHierarchy()){
            res=Result.fail("The component is currently not visible in the hierarchy and thus you can not be used. Component: "+component);
        }
        if(!component.isEnabledInHierarchy()){
            res=Result.fail("The component is currently not enabled in the hierarchy and thus you can not be used. Component: "+component);
        }
        if(throwException&&res.wasFailed()){
            throw new AssertionFailedError(res.getMessage());
        }
        return res;
    }
    public RequestCycle getRequestCycle(){
        return this.requestCycle;
    }
    public MockHttpServletResponse getResponse(){
        return this.response;
    }
    public MockHttpServletRequest getLastRequest(){
        return this.lastRequest;
    }
    public boolean isExposeExceptions(){
        return this.exposeExceptions;
    }
    public void setExposeExceptions(final boolean exposeExceptions){
        this.exposeExceptions=exposeExceptions;
    }
    public boolean isUseRequestUrlAsBase(){
        return this.useRequestUrlAsBase;
    }
    public void setUseRequestUrlAsBase(final boolean setBaseUrl){
        this.useRequestUrlAsBase=setBaseUrl;
    }
    public void executeUrl(final String _url){
        final Url url=Url.parse(_url,Charset.forName(this.request.getCharacterEncoding()));
        this.transform(url);
        this.getRequest().setUrl(url);
        this.processRequest();
    }
    static{
        log=LoggerFactory.getLogger(BaseWicketTester.class);
    }
    public static class StartComponentInPage extends WebPage{
        private transient IMarkupFragment pageMarkup;
        public StartComponentInPage(){
            super();
            this.pageMarkup=null;
            this.setStatelessHint(false);
        }
        public IMarkupFragment getMarkup(){
            IMarkupFragment calculatedMarkup=null;
            if(this.pageMarkup==null){
                final IMarkupFragment markup=super.getMarkup();
                if(markup!=null&&markup!=Markup.NO_MARKUP){
                    calculatedMarkup=markup;
                    this.pageMarkup=markup;
                }
            }
            else{
                calculatedMarkup=this.pageMarkup;
            }
            return calculatedMarkup;
        }
    }
    private class LastPageRecordingPageRendererProvider implements IPageRendererProvider{
        private final IPageRendererProvider delegate;
        private Page lastPage;
        public LastPageRecordingPageRendererProvider(final IPageRendererProvider delegate){
            super();
            this.delegate=delegate;
        }
        public PageRenderer get(final RenderPageRequestHandler handler){
            final Page newPage=(Page)handler.getPageProvider().getPageInstance();
            if(BaseWicketTester.this.componentInPage!=null&&this.lastPage!=null&&this.lastPage.getPageClass()!=newPage.getPageClass()){
                BaseWicketTester.this.componentInPage=null;
            }
            BaseWicketTester.this.lastRenderedPage=(this.lastPage=newPage);
            return (PageRenderer)this.delegate.get((Object)handler);
        }
    }
    private class TestExceptionMapper implements IExceptionMapper{
        private final IExceptionMapper delegate;
        public TestExceptionMapper(final IExceptionMapper delegate){
            super();
            this.delegate=delegate;
        }
        public IRequestHandler map(final Exception e){
            if(!BaseWicketTester.this.exposeExceptions){
                return this.delegate.map(e);
            }
            if(e instanceof RuntimeException){
                throw (RuntimeException)e;
            }
            throw new WicketRuntimeException(e);
        }
    }
    private class TestRequestCycleProvider implements IRequestCycleProvider{
        private final IRequestCycleProvider delegate;
        public TestRequestCycleProvider(final IRequestCycleProvider delegate){
            super();
            this.delegate=delegate;
        }
        public RequestCycle get(final RequestCycleContext context){
            context.setRequestMapper((IRequestMapper)new TestRequestMapper(context.getRequestMapper()));
            BaseWicketTester.this.forcedHandler=null;
            context.setExceptionMapper((IExceptionMapper)new TestExceptionMapper(context.getExceptionMapper()));
            return (RequestCycle)this.delegate.get((Object)context);
        }
    }
    private class TestRequestMapper implements IRequestMapper{
        private final IRequestMapper delegate;
        public TestRequestMapper(final IRequestMapper delegate){
            super();
            this.delegate=delegate;
        }
        public int getCompatibilityScore(final Request request){
            return this.delegate.getCompatibilityScore(request);
        }
        public Url mapHandler(final IRequestHandler requestHandler){
            return this.delegate.mapHandler(requestHandler);
        }
        public IRequestHandler mapRequest(final Request request){
            if(BaseWicketTester.this.forcedHandler!=null){
                final IRequestHandler handler=BaseWicketTester.this.forcedHandler;
                BaseWicketTester.this.forcedHandler=null;
                return handler;
            }
            return this.delegate.mapRequest(request);
        }
    }
    private static class TestPageManagerProvider implements IPageManagerProvider{
        public IPageManager get(final IPageManagerContext pageManagerContext){
            return new MockPageManager();
        }
    }
    private class TestFilterConfig implements FilterConfig{
        private final Map<String,String> initParameters;
        public TestFilterConfig(){
            super();
            (this.initParameters=(Map<String,String>)new HashMap()).put("filterMappingUrlPattern","/servlet/*");
        }
        public String getFilterName(){
            return this.getClass().getName();
        }
        public ServletContext getServletContext(){
            return BaseWicketTester.this.servletContext;
        }
        public String getInitParameter(final String s){
            return (String)this.initParameters.get(s);
        }
        public Enumeration<String> getInitParameterNames(){
            throw new UnsupportedOperationException("Not implemented");
        }
    }
    private static class WicketTesterServletWebResponse extends ServletWebResponse implements IMetaDataBufferingWebResponse{
        private List<Cookie> cookies;
        public WicketTesterServletWebResponse(final ServletWebRequest request,final MockHttpServletResponse response){
            super(request,response);
            this.cookies=(List<Cookie>)new ArrayList();
        }
        public void addCookie(final Cookie cookie){
            super.addCookie(cookie);
            this.cookies.add(cookie);
        }
        public void writeMetaData(final WebResponse webResponse){
            for(final Cookie cookie : this.cookies){
                webResponse.addCookie(cookie);
            }
        }
        public void sendRedirect(final String url){
            super.sendRedirect(url);
            try{
                this.getContainerResponse().sendRedirect(url);
            }
            catch(IOException e){
                throw new RuntimeException((Throwable)e);
            }
        }
    }
}
