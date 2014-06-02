package org.apache.wicket.ajax;

import org.apache.wicket.request.handler.logger.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.*;
import org.apache.wicket.event.*;
import org.apache.wicket.response.*;
import org.apache.wicket.response.filter.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;
import org.slf4j.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.request.resource.*;
import java.util.*;

public class AjaxRequestTarget implements IPageRequestHandler,ILoggableRequestHandler{
    private static final Logger log;
    private final List<CharSequence> appendJavaScripts;
    private final List<CharSequence> domReadyJavaScripts;
    private final AjaxResponse encodingBodyResponse;
    private final AjaxResponse encodingHeaderResponse;
    private final Map<String,Component> markupIdToComponent;
    private final List<CharSequence> prependJavaScripts;
    private List<IListener> listeners;
    private final Set<ITargetRespondListener> respondListeners;
    private final Page page;
    private transient boolean componentsFrozen;
    private transient boolean listenersFrozen;
    private transient boolean respondersFrozen;
    private PageLogData logData;
    private boolean headerRendering;
    private HtmlHeaderContainer header;
    private IHeaderResponse headerResponse;
    public AjaxRequestTarget(final Page page){
        super();
        this.appendJavaScripts=(List<CharSequence>)Generics.newArrayList();
        this.domReadyJavaScripts=(List<CharSequence>)Generics.newArrayList();
        this.markupIdToComponent=(Map<String,Component>)new LinkedHashMap();
        this.prependJavaScripts=(List<CharSequence>)Generics.newArrayList();
        this.listeners=null;
        this.respondListeners=(Set<ITargetRespondListener>)new HashSet();
        this.headerRendering=false;
        this.header=null;
        Args.notNull((Object)page,"page");
        this.page=page;
        final Response response=RequestCycle.get().getResponse();
        this.encodingBodyResponse=new AjaxResponse(response);
        this.encodingHeaderResponse=new AjaxResponse(response);
    }
    public Page getPage(){
        return this.page;
    }
    private void assertNotFrozen(final boolean frozen,final Class<?> clazz){
        if(frozen){
            throw new IllegalStateException(clazz.getSimpleName()+"s can no "+" longer be added");
        }
    }
    private void assertListenersNotFrozen(){
        this.assertNotFrozen(this.listenersFrozen,(Class<?>)IListener.class);
    }
    private void assertComponentsNotFrozen(){
        this.assertNotFrozen(this.componentsFrozen,(Class<?>)Component.class);
    }
    private void assertRespondersNotFrozen(){
        this.assertNotFrozen(this.respondersFrozen,(Class<?>)ITargetRespondListener.class);
    }
    public void addListener(final IListener listener) throws IllegalStateException{
        Args.notNull((Object)listener,"listener");
        this.assertListenersNotFrozen();
        if(this.listeners==null){
            this.listeners=(List<IListener>)new LinkedList();
        }
        if(!this.listeners.contains(listener)){
            this.listeners.add(listener);
        }
    }
    public final void addChildren(final MarkupContainer parent,final Class<?> childCriteria){
        Args.notNull((Object)parent,"parent");
        Args.notNull((Object)childCriteria,"childCriteria");
        parent.visitChildren(childCriteria,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                AjaxRequestTarget.this.add(component);
                visit.dontGoDeeper();
            }
        });
    }
    @Deprecated
    public void addComponent(final Component... components){
        this.add(components);
    }
    public void add(final Component... components){
        for(final Component component : components){
            Args.notNull((Object)component,"component");
            if(!component.getOutputMarkupId()&&!(component instanceof Page)){
                throw new IllegalArgumentException("cannot update component that does not have setOutputMarkupId property set to true. Component: "+component.toString());
            }
            this.add(component,component.getMarkupId());
        }
    }
    @Deprecated
    public final void addComponent(final Component component,final String markupId){
        this.add(component,markupId);
    }
    public final void add(final Component component,final String markupId) throws IllegalArgumentException,IllegalStateException{
        Args.notEmpty((CharSequence)markupId,"markupId");
        Args.notNull((Object)component,"component");
        if(component instanceof Page){
            if(component!=this.page){
                throw new IllegalArgumentException("component cannot be a page");
            }
        }
        else if(component instanceof AbstractRepeater){
            throw new IllegalArgumentException("Component "+component.getClass().getName()+" has been added to the target. This component is a repeater and cannot be repainted via ajax directly. "+"Instead add its parent or another markup container higher in the hierarchy.");
        }
        this.assertComponentsNotFrozen();
        component.setMarkupId(markupId);
        this.markupIdToComponent.put(markupId,component);
    }
    public final Collection<? extends Component> getComponents(){
        return (Collection<? extends Component>)Collections.unmodifiableCollection(this.markupIdToComponent.values());
    }
    public final void focusComponent(final Component component){
        if(component!=null&&!component.getOutputMarkupId()){
            throw new IllegalArgumentException("cannot update component that does not have setOutputMarkupId property set to true. Component: "+component.toString());
        }
        final String id=(component!=null)?("'"+component.getMarkupId()+"'"):"null";
        this.appendJavaScript((CharSequence)("Wicket.Focus.setFocusOnId("+id+");"));
    }
    public final void appendJavaScript(final CharSequence javascript){
        Args.notNull((Object)javascript,"javascript");
        this.appendJavaScripts.add(javascript);
    }
    public void detach(final IRequestCycle requestCycle){
        if(this.logData==null){
            this.logData=new PageLogData(this.page);
        }
        if(this.markupIdToComponent.size()>0){
            final Component component=(Component)this.markupIdToComponent.values().iterator().next();
            final Page page=component.findParent((Class<Page>)Page.class);
            if(page!=null){
                page.detach();
            }
        }
    }
    public boolean equals(final Object obj){
        if(obj instanceof AjaxRequestTarget){
            final AjaxRequestTarget that=(AjaxRequestTarget)obj;
            return this.markupIdToComponent.equals(that.markupIdToComponent)&&this.prependJavaScripts.equals(that.prependJavaScripts)&&this.appendJavaScripts.equals(that.appendJavaScripts);
        }
        return false;
    }
    public int hashCode(){
        int result="AjaxRequestTarget".hashCode();
        result+=this.markupIdToComponent.hashCode()*17;
        result+=this.prependJavaScripts.hashCode()*17;
        result+=this.appendJavaScripts.hashCode()*17;
        return result;
    }
    public final void prependJavaScript(final CharSequence javascript){
        Args.notNull((Object)javascript,"javascript");
        this.prependJavaScripts.add(javascript);
    }
    public void registerRespondListener(final ITargetRespondListener listener){
        this.assertRespondersNotFrozen();
        this.respondListeners.add(listener);
    }
    public final void respond(final IRequestCycle requestCycle){
        final RequestCycle rc=(RequestCycle)requestCycle;
        final WebResponse response=(WebResponse)requestCycle.getResponse();
        if(this.markupIdToComponent.values().contains(this.page)){
            final IRequestHandler handler=(IRequestHandler)new RenderPageRequestHandler(new PageProvider(this.page));
            final String url=rc.urlFor(handler).toString();
            response.sendRedirect(url);
            return;
        }
        this.respondersFrozen=true;
        for(final ITargetRespondListener listener : this.respondListeners){
            listener.onTargetRespond(this);
        }
        final Application app=Application.get();
        this.page.send(app,Broadcast.BREADTH,this);
        final String encoding=app.getRequestCycleSettings().getResponseRequestEncoding();
        response.setContentType("text/xml; charset="+encoding);
        response.disableCaching();
        try{
            final StringResponse bodyResponse=new StringResponse();
            this.constructResponseBody(bodyResponse,encoding);
            final CharSequence filteredResponse=(CharSequence)this.invokeResponseFilters(bodyResponse);
            response.write(filteredResponse);
        }
        finally{
            RequestCycle.get().setResponse((Response)response);
        }
    }
    private void constructResponseBody(final Response bodyResponse,final String encoding){
        bodyResponse.write((CharSequence)"<?xml version=\"1.0\" encoding=\"");
        bodyResponse.write((CharSequence)encoding);
        bodyResponse.write((CharSequence)"\"?>");
        bodyResponse.write((CharSequence)"<ajax-response>");
        this.fireOnBeforeRespondListeners();
        this.respondComponents(bodyResponse);
        this.fireOnAfterRespondListeners(bodyResponse);
        for(final CharSequence js : this.prependJavaScripts){
            this.respondPriorityInvocation(bodyResponse,js);
        }
        for(final CharSequence js : this.domReadyJavaScripts){
            this.respondInvocation(bodyResponse,js);
        }
        for(final CharSequence js : this.appendJavaScripts){
            this.respondInvocation(bodyResponse,js);
        }
        bodyResponse.write((CharSequence)"</ajax-response>");
    }
    private AppendingStringBuffer invokeResponseFilters(final StringResponse contentResponse){
        AppendingStringBuffer responseBuffer=new AppendingStringBuffer(contentResponse.getBuffer());
        final List<IResponseFilter> responseFilters=Application.get().getRequestCycleSettings().getResponseFilters();
        if(responseFilters!=null){
            for(final IResponseFilter filter : responseFilters){
                responseBuffer=filter.filter(responseBuffer);
            }
        }
        return responseBuffer;
    }
    private void fireOnBeforeRespondListeners(){
        this.listenersFrozen=true;
        if(this.listeners!=null){
            final Map<String,Component> components=(Map<String,Component>)Collections.unmodifiableMap(this.markupIdToComponent);
            for(final IListener listener : this.listeners){
                listener.onBeforeRespond(components,this);
            }
        }
        this.listenersFrozen=false;
    }
    private void fireOnAfterRespondListeners(final Response response){
        this.listenersFrozen=true;
        if(this.listeners!=null){
            final Map<String,Component> components=(Map<String,Component>)Collections.unmodifiableMap(this.markupIdToComponent);
            final IJavaScriptResponse jsresponse=new IJavaScriptResponse(){
                public void addJavaScript(final String script){
                    AjaxRequestTarget.this.respondInvocation(response,(CharSequence)script);
                }
            };
            for(final IListener listener : this.listeners){
                listener.onAfterRespond(components,jsresponse);
            }
        }
    }
    private void respondComponents(final Response response){
        this.componentsFrozen=true;
        for(final Map.Entry<String,Component> stringComponentEntry : this.markupIdToComponent.entrySet()){
            final Component component=(Component)stringComponentEntry.getValue();
            if(!this.containsAncestorFor(component)){
                this.respondComponent(response,component.getAjaxRegionMarkupId(),component);
            }
        }
        if(this.header!=null){
            this.headerRendering=true;
            final Response oldResponse=RequestCycle.get().setResponse(this.encodingHeaderResponse);
            this.encodingHeaderResponse.reset();
            this.header.getHeaderResponse().close();
            RequestCycle.get().setResponse(oldResponse);
            this.writeHeaderContribution(response);
            this.headerRendering=false;
        }
    }
    private void writeHeaderContribution(final Response response){
        if(this.encodingHeaderResponse.getContents().length()!=0){
            response.write((CharSequence)"<header-contribution");
            if(this.encodingHeaderResponse.isContentsEncoded()){
                response.write((CharSequence)" encoding=\"");
                response.write((CharSequence)this.getEncodingName());
                response.write((CharSequence)"\" ");
            }
            response.write((CharSequence)"><![CDATA[<head xmlns:wicket=\"http://wicket.apache.org\">");
            response.write(this.encodingHeaderResponse.getContents());
            response.write((CharSequence)"</head>]]>");
            response.write((CharSequence)"</header-contribution>");
        }
    }
    private boolean containsAncestorFor(final Component component){
        for(Component cursor=component.getParent();cursor!=null;cursor=cursor.getParent()){
            if(this.markupIdToComponent.containsValue(cursor)){
                return true;
            }
        }
        return false;
    }
    public String toString(){
        return "[AjaxRequestTarget@"+this.hashCode()+" markupIdToComponent ["+this.markupIdToComponent+"], prependJavaScript ["+this.prependJavaScripts+"], appendJavaScript ["+this.appendJavaScripts+"]";
    }
    protected String encode(final CharSequence str){
        if(str==null){
            return null;
        }
        return Strings.replaceAll(str,(CharSequence)"]",(CharSequence)"]^").toString();
    }
    protected String getEncodingName(){
        return "wicket1";
    }
    protected boolean needsEncoding(final CharSequence str){
        return Strings.indexOf(str,']')>=0;
    }
    private void respondComponent(final Response response,final String markupId,final Component component){
        if(component.getRenderBodyOnly()){
            throw new IllegalStateException("Ajax render cannot be called on component that has setRenderBodyOnly enabled. Component: "+component.toString());
        }
        component.setOutputMarkupId(true);
        this.encodingBodyResponse.reset();
        RequestCycle.get().setResponse(this.encodingBodyResponse);
        final Page page=component.findParent((Class<Page>)Page.class);
        if(page==null){
            AjaxRequestTarget.log.debug("component: "+component+" with markupid: "+markupId+" not rendered because it was already removed from page");
            return;
        }
        page.startComponentRender(component);
        try{
            component.prepareForRender();
            this.respondHeaderContribution(response,component);
        }
        catch(RuntimeException e){
            try{
                component.afterRender();
            }
            catch(RuntimeException ex){
            }
            RequestCycle.get().setResponse(response);
            this.encodingBodyResponse.reset();
            throw e;
        }
        try{
            component.render();
        }
        catch(RuntimeException e){
            RequestCycle.get().setResponse(response);
            this.encodingBodyResponse.reset();
            throw e;
        }
        page.endComponentRender(component);
        RequestCycle.get().setResponse(response);
        response.write((CharSequence)"<component id=\"");
        response.write((CharSequence)markupId);
        response.write((CharSequence)"\" ");
        if(this.encodingBodyResponse.isContentsEncoded()){
            response.write((CharSequence)" encoding=\"");
            response.write((CharSequence)this.getEncodingName());
            response.write((CharSequence)"\" ");
        }
        response.write((CharSequence)"><![CDATA[");
        response.write(this.encodingBodyResponse.getContents());
        response.write((CharSequence)"]]></component>");
        this.encodingBodyResponse.reset();
    }
    public IHeaderResponse getHeaderResponse(){
        if(this.headerResponse==null){
            this.headerResponse=new AjaxHeaderResponse();
        }
        return this.headerResponse;
    }
    private void respondHeaderContribution(final Response response,final Component component){
        this.headerRendering=true;
        if(this.header==null){
            this.header=new AjaxHtmlHeaderContainer("_header_",this);
            final Page page=component.getPage();
            page.addOrReplace(this.header);
        }
        final Response oldResponse=RequestCycle.get().setResponse(this.encodingHeaderResponse);
        this.encodingHeaderResponse.reset();
        component.renderHead(this.header);
        if(component instanceof MarkupContainer){
            ((MarkupContainer)component).visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    if(component.isVisibleInHierarchy()){
                        component.renderHead(AjaxRequestTarget.this.header);
                    }
                    else{
                        visit.dontGoDeeper();
                    }
                }
            });
        }
        RequestCycle.get().setResponse(oldResponse);
        this.writeHeaderContribution(response);
        this.headerRendering=false;
    }
    private void respondInvocation(final Response response,final CharSequence js){
        this.respondJavascriptInvocation("evaluate",response,js);
    }
    private void respondPriorityInvocation(final Response response,final CharSequence js){
        this.respondJavascriptInvocation("priority-evaluate",response,js);
    }
    private void respondJavascriptInvocation(final String invocation,final Response response,final CharSequence js){
        boolean encoded=false;
        CharSequence javascript=js;
        if(this.needsEncoding(js)){
            encoded=true;
            javascript=(CharSequence)this.encode(js);
        }
        response.write((CharSequence)"<");
        response.write((CharSequence)invocation);
        if(encoded){
            response.write((CharSequence)" encoding=\"");
            response.write((CharSequence)this.getEncodingName());
            response.write((CharSequence)"\"");
        }
        response.write((CharSequence)">");
        response.write((CharSequence)"<![CDATA[");
        response.write(javascript);
        response.write((CharSequence)"]]>");
        response.write((CharSequence)"</");
        response.write((CharSequence)invocation);
        response.write((CharSequence)">");
        this.encodingBodyResponse.reset();
    }
    public static AjaxRequestTarget get(){
        final RequestCycle requestCycle=RequestCycle.get();
        if(requestCycle!=null){
            if(requestCycle.getActiveRequestHandler() instanceof AjaxRequestTarget){
                return (AjaxRequestTarget)requestCycle.getActiveRequestHandler();
            }
            if(requestCycle.getRequestHandlerScheduledAfterCurrent() instanceof AjaxRequestTarget){
                return (AjaxRequestTarget)requestCycle.getRequestHandlerScheduledAfterCurrent();
            }
        }
        return null;
    }
    public String getLastFocusedElementId(){
        final WebRequest request=(WebRequest)RequestCycle.get().getRequest();
        final String id=request.getHeader("Wicket-FocusedElementId");
        return Strings.isEmpty((CharSequence)id)?null:id;
    }
    public Class<? extends IRequestablePage> getPageClass(){
        return this.page.getPageClass();
    }
    public Integer getPageId(){
        return this.page.getPageId();
    }
    public PageParameters getPageParameters(){
        return this.page.getPageParameters();
    }
    public final boolean isPageInstanceCreated(){
        return true;
    }
    public final Integer getRenderCount(){
        return this.page.getRenderCount();
    }
    public PageLogData getLogData(){
        return this.logData;
    }
    static{
        log=LoggerFactory.getLogger(AjaxRequestTarget.class);
    }
    private final class AjaxResponse extends Response{
        private final AppendingStringBuffer buffer;
        private boolean escaped;
        private final Response originalResponse;
        public AjaxResponse(final Response originalResponse){
            super();
            this.buffer=new AppendingStringBuffer(256);
            this.escaped=false;
            this.originalResponse=originalResponse;
        }
        public String encodeURL(final CharSequence url){
            return this.originalResponse.encodeURL(url);
        }
        public CharSequence getContents(){
            return (CharSequence)this.buffer;
        }
        public boolean isContentsEncoded(){
            return this.escaped;
        }
        public void write(final CharSequence cs){
            String string=cs.toString();
            if(AjaxRequestTarget.this.needsEncoding((CharSequence)string)){
                string=AjaxRequestTarget.this.encode((CharSequence)string);
                this.escaped=true;
                this.buffer.append(string);
            }
            else{
                this.buffer.append((Object)cs);
            }
        }
        public void reset(){
            this.buffer.clear();
            this.escaped=false;
        }
        public void write(final byte[] array){
            throw new UnsupportedOperationException("Cannot write binary data.");
        }
        public void write(final byte[] array,final int offset,final int length){
            throw new UnsupportedOperationException("Cannot write binary data.");
        }
        public Object getContainerResponse(){
            return this.originalResponse.getContainerResponse();
        }
    }
    private class AjaxHeaderResponse extends HeaderResponse{
        private boolean checkHeaderRendering(){
            if(!AjaxRequestTarget.this.headerRendering){
                AjaxRequestTarget.log.debug("Only methods that can be called on IHeaderResponse outside renderHead() are renderOnLoadJavaScript and renderOnDomReadyJavaScript");
            }
            return AjaxRequestTarget.this.headerRendering;
        }
        public void renderCSSReference(final ResourceReference reference,final String media){
            if(this.checkHeaderRendering()){
                super.renderCSSReference(reference,media);
            }
        }
        public void renderCSSReference(final String url){
            if(this.checkHeaderRendering()){
                super.renderCSSReference(url);
            }
        }
        public void renderCSSReference(final String url,final String media){
            if(this.checkHeaderRendering()){
                super.renderCSSReference(url,media);
            }
        }
        public void renderJavaScript(final CharSequence javascript,final String id){
            if(this.checkHeaderRendering()){
                super.renderJavaScript(javascript,id);
            }
        }
        public void renderCSSReference(final ResourceReference reference){
            if(this.checkHeaderRendering()){
                super.renderCSSReference(reference);
            }
        }
        public void renderJavaScriptReference(final ResourceReference reference){
            if(this.checkHeaderRendering()){
                super.renderJavaScriptReference(reference);
            }
        }
        public void renderJavaScriptReference(final ResourceReference reference,final String id){
            if(this.checkHeaderRendering()){
                super.renderJavaScriptReference(reference,id);
            }
        }
        public void renderJavaScriptReference(final String url){
            if(this.checkHeaderRendering()){
                super.renderJavaScriptReference(url);
            }
        }
        public void renderJavaScriptReference(final String url,final String id){
            if(this.checkHeaderRendering()){
                super.renderJavaScriptReference(url,id);
            }
        }
        public void renderString(final CharSequence string){
            if(this.checkHeaderRendering()){
                super.renderString(string);
            }
        }
        public void renderOnDomReadyJavaScript(final String javascript){
            final List<String> token=(List<String>)Arrays.asList(new String[] { "javascript-event","window","domready",javascript });
            if(!this.wasRendered(token)){
                AjaxRequestTarget.this.domReadyJavaScripts.add(javascript);
                this.markRendered(token);
            }
        }
        public void renderOnLoadJavaScript(final String javascript){
            final List<String> token=(List<String>)Arrays.asList(new String[] { "javascript-event","window","load",javascript });
            if(!this.wasRendered(token)){
                AjaxRequestTarget.this.appendJavaScripts.add(javascript);
                this.markRendered(token);
            }
        }
        protected Response getRealResponse(){
            return RequestCycle.get().getResponse();
        }
    }
    private static class AjaxHtmlHeaderContainer extends HtmlHeaderContainer{
        private static final long serialVersionUID=1L;
        private final transient AjaxRequestTarget target;
        public AjaxHtmlHeaderContainer(final String id,final AjaxRequestTarget target){
            super(id);
            this.target=target;
        }
        protected IHeaderResponse newHeaderResponse(){
            return this.target.getHeaderResponse();
        }
    }
    public interface ITargetRespondListener{
        void onTargetRespond(AjaxRequestTarget p0);
    }
    public interface IJavaScriptResponse{
        void addJavaScript(String p0);
    }
    public interface IListener{
        void onBeforeRespond(Map<String,Component> p0,AjaxRequestTarget p1);
        void onAfterRespond(Map<String,Component> p0,IJavaScriptResponse p1);
    }
}
