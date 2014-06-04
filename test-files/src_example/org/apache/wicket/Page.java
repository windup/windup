package org.apache.wicket;

import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.model.*;
import org.apache.wicket.page.*;
import java.io.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.settings.*;
import java.util.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public abstract class Page extends MarkupContainer implements IRedirectListener,IRequestablePage{
    private static final int FLAG_IS_DIRTY=1024;
    private static final int FLAG_PREVENT_DIRTY=2048;
    private static final int FLAG_STATELESS_HINT=65536;
    private static final int FLAG_WAS_CREATED_BOOKMARKABLE=524288;
    private static final Logger log;
    private static final long serialVersionUID=1L;
    private int autoIndex;
    private int numericId;
    private transient Set<Component> renderedComponents;
    private transient Boolean stateless;
    private final PageParameters pageParameters;
    private int renderCount;
    protected Page(){
        this((PageParameters)null,null);
    }
    protected Page(final IModel<?> model){
        this((PageParameters)null,model);
    }
    protected Page(final PageParameters parameters){
        this(parameters,null);
    }
    private Page(final PageParameters parameters,final IModel<?> model){
        super(null,model);
        this.stateless=null;
        this.renderCount=0;
        if(parameters==null){
            this.pageParameters=new PageParameters();
        }
        else{
            this.pageParameters=parameters;
        }
        this.init();
    }
    public PageParameters getPageParameters(){
        return this.pageParameters;
    }
    public final void componentRendered(final Component component){
        if(this.getApplication().getDebugSettings().getComponentUseCheck()){
            if(this.renderedComponents==null){
                this.renderedComponents=(Set<Component>)new HashSet();
            }
            if(!this.renderedComponents.add(component)){
                throw new MarkupException("The component "+component+" was rendered already. You can render it only once during a render phase. Class relative path: "+component.getClassRelativePath());
            }
            if(Page.log.isDebugEnabled()){
                Page.log.debug("Rendered "+component);
            }
        }
    }
    public void detachModels(){
        super.detachModels();
    }
    public void internalPrepareForRender(final boolean setRenderingFlag){
        if(!this.isInitialized()){
            this.internalInitialize();
        }
        super.internalPrepareForRender(setRenderingFlag);
    }
    public final void dirty(){
        this.dirty(false);
    }
    public boolean setFreezePageId(final boolean freeze){
        final boolean frozen=this.getFlag(2048);
        this.setFlag(2048,freeze);
        return frozen;
    }
    public void dirty(final boolean isInitialization){
        this.checkHierarchyChange(this);
        if(this.getFlag(2048)){
            return;
        }
        final IPageManager pageManager=this.getSession().getPageManager();
        if(!this.getFlag(1024)&&((this.isVersioned()&&pageManager.supportsVersioning())||isInitialization)){
            this.setFlag(1024,true);
            this.setNextAvailableId();
            pageManager.touchPage(this);
        }
    }
    public final void endComponentRender(final Component component){
        if(component instanceof MarkupContainer){
            this.checkRendering((MarkupContainer)component);
        }
        else{
            this.renderedComponents=null;
        }
    }
    public final int getAutoIndex(){
        return this.autoIndex++;
    }
    public final String getId(){
        return Integer.toString(this.numericId);
    }
    public final Class<? extends Page> getPageClass(){
        return (Class<? extends Page>)this.getClass();
    }
    public final long getSizeInBytes(){
        return WicketObjects.sizeof((Serializable)this);
    }
    public final boolean getStatelessHint(){
        return this.getFlag(65536);
    }
    public final String hierarchyAsString(){
        final StringBuilder buffer=new StringBuilder();
        buffer.append("Page ").append(this.getId());
        this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                int levels=0;
                for(Component current=component;current!=null;current=current.getParent()){
                    ++levels;
                }
                buffer.append(StringValue.repeat(levels,"\t")).append(component.getPageRelativePath()).append(":").append(Classes.simpleName(component.getClass()));
            }
        });
        return buffer.toString();
    }
    public boolean isBookmarkable(){
        return this.getApplication().getPageFactory().isBookmarkable((Class<IRequestablePage>)this.getClass());
    }
    public boolean isErrorPage(){
        return false;
    }
    private boolean peekPageStateless(){
        final Boolean old=this.stateless;
        final Boolean res=this.isPageStateless();
        this.stateless=old;
        return res;
    }
    public final boolean isPageStateless(){
        if(!this.isBookmarkable()){
            this.stateless=Boolean.FALSE;
            if(this.getStatelessHint()){
                Page.log.warn("Page '"+this+"' is not stateless because it is not bookmarkable, "+"but the stateless hint is set to true!");
            }
        }
        if(!this.getStatelessHint()){
            return false;
        }
        if(this.stateless==null&&!this.isStateless()){
            this.stateless=Boolean.FALSE;
        }
        if(this.stateless==null){
            final Component statefulComponent=this.visitChildren((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Component>)new IVisitor<Component,Component>(){
                public void component(final Component component,final IVisit<Component> visit){
                    if(!component.isStateless()){
                        visit.stop((Object)component);
                    }
                }
            });
            this.stateless=(statefulComponent==null);
            if(Page.log.isDebugEnabled()&&!this.stateless&&this.getStatelessHint()){
                Page.log.debug("Page '{}' is not stateless because of component with path '{}'.",this,statefulComponent.getPageRelativePath());
            }
        }
        return this.stateless;
    }
    public final void onRedirect(){
    }
    public final void setNumericId(final int id){
        this.numericId=id;
    }
    public final void setStatelessHint(final boolean value){
        if(value&&!this.isBookmarkable()){
            throw new WicketRuntimeException("Can't set stateless hint to true on a page when the page is not bookmarkable, page: "+this);
        }
        this.setFlag(65536,value);
    }
    public final void startComponentRender(final Component component){
        this.renderedComponents=null;
    }
    public String toString(){
        return "[Page class = "+this.getClass().getName()+", id = "+this.getId()+", render count = "+this.getRenderCount()+"]";
    }
    private void checkRendering(final MarkupContainer renderedContainer){
        final IDebugSettings debugSettings=this.getApplication().getDebugSettings();
        if(debugSettings.getComponentUseCheck()){
            final List<Component> unrenderedComponents=(List<Component>)new ArrayList();
            final StringBuilder buffer=new StringBuilder();
            renderedContainer.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    if(Page.this.renderedComponents==null||!Page.this.renderedComponents.contains(component)){
                        if(!component.isAuto()&&component.isVisibleInHierarchy()){
                            unrenderedComponents.add(component);
                            buffer.append(Integer.toString(unrenderedComponents.size())).append(". ").append(component).append('\n');
                            String metadata=component.getMetaData(Component.CONSTRUCTED_AT_KEY);
                            if(metadata!=null){
                                buffer.append(metadata);
                            }
                            metadata=component.getMetaData(Component.ADDED_AT_KEY);
                            if(metadata!=null){
                                buffer.append(metadata);
                            }
                        }
                        else{
                            visit.dontGoDeeper();
                        }
                    }
                }
            });
            if(unrenderedComponents.size()>0){
                this.renderedComponents=null;
                final List<Component> transparentContainerChildren=(List<Component>)Generics.newArrayList();
                final Iterator<Component> iterator=(Iterator<Component>)unrenderedComponents.iterator();
            Label_0077:
                while(iterator.hasNext()){
                    final Component component=(Component)iterator.next();
                    for(final Component transparentContainerChild : transparentContainerChildren){
                        for(MarkupContainer parent=component.getParent();parent!=null;parent=parent.getParent()){
                            if(parent==transparentContainerChild){
                                iterator.remove();
                                continue Label_0077;
                            }
                        }
                    }
                    if(this.hasInvisibleTransparentChild(component.getParent(),component)){
                        if(Page.log.isDebugEnabled()){
                            Page.log.debug("Component {} wasn't rendered but might have a transparent parent.",component);
                        }
                        transparentContainerChildren.add(component);
                        iterator.remove();
                    }
                }
                if(unrenderedComponents.size()>0){
                    throw new WicketRuntimeException("The component(s) below failed to render. Possible reasons could be that: 1) you have added a component in code but forgot to reference it in the markup (thus the component will never be rendered), 2) if your components were added in a parent container then make sure the markup for the child container includes them in <wicket:extend>.\n\n"+buffer.toString());
                }
            }
        }
        this.renderedComponents=null;
    }
    private boolean hasInvisibleTransparentChild(final MarkupContainer root,final Component self){
        for(final Component sibling : root){
            if(sibling!=self&&sibling instanceof IComponentResolver&&sibling instanceof MarkupContainer){
                if(!sibling.isVisible()){
                    return true;
                }
                final boolean rtn=this.hasInvisibleTransparentChild((MarkupContainer)sibling,self);
                if(rtn){
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    private void init(){
        if(!this.isBookmarkable()){
            this.setStatelessHint(false);
        }
        this.setVersioned(this.getApplication().getPageSettings().getVersionPagesByDefault());
        this.dirty(true);
        this.stateless=null;
    }
    private void setNextAvailableId(){
        this.setNumericId(this.getSession().nextPageId());
    }
    protected void componentChanged(final Component component,final MarkupContainer parent){
        if(!component.isAuto()){
            this.dirty();
        }
    }
    protected final void internalOnModelChanged(){
        this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                if(component.sameInnermostModel(Page.this)){
                    component.modelChanged();
                }
            }
        });
    }
    void internalOnAfterConfigure(){
        super.internalOnAfterConfigure();
        if(!this.isActionAuthorized(Page.RENDER)){
            if(Page.log.isDebugEnabled()){
                Page.log.debug("Page not allowed to render: "+this);
            }
            throw new UnauthorizedActionException(this,Component.RENDER);
        }
    }
    protected void onBeforeRender(){
        this.renderedComponents=null;
        if(Boolean.TRUE.equals(this.stateless)){
            this.stateless=null;
        }
        super.onBeforeRender();
        if(this.getSession().isTemporary()&&!this.peekPageStateless()){
            this.getSession().bind();
        }
    }
    protected void onAfterRender(){
        super.onAfterRender();
        this.checkRendering(this);
        if(this.getApplication().getDebugSettings().getComponentUseCheck()){
            this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    component.setMetaData(Component.CONSTRUCTED_AT_KEY,null);
                    component.setMetaData(Component.ADDED_AT_KEY,null);
                }
            });
        }
        if(!this.isPageStateless()){
            this.getSession().getSessionStore().getSessionId(RequestCycle.get().getRequest(),true);
            this.getSession().getPageManager().touchPage(this);
        }
        if(this.getApplication().getDebugSettings().isOutputMarkupContainerClassName()){
            Class<?> klass;
            for(klass=(Class<?>)this.getClass();klass.isAnonymousClass();klass=(Class<?>)klass.getSuperclass()){
            }
            this.getResponse().write((CharSequence)"<!-- Page Class ");
            this.getResponse().write((CharSequence)klass.getName());
            this.getResponse().write((CharSequence)" END -->\n");
        }
    }
    protected void onDetach(){
        if(Page.log.isDebugEnabled()){
            Page.log.debug("ending request for page "+this+", request "+this.getRequest());
        }
        this.setFlag(1024,false);
        super.onDetach();
    }
    protected void onRender(){
        final MarkupStream markupStream=new MarkupStream(this.getMarkup());
        this.renderAll(markupStream,null);
    }
    final void componentAdded(final Component component){
        if(!component.isAuto()){
            this.dirty();
        }
    }
    final void componentModelChanging(final Component component){
        this.dirty();
    }
    final void componentRemoved(final Component component){
        if(!component.isAuto()){
            this.dirty();
        }
    }
    final void componentStateChanging(final Component component){
        if(!component.isAuto()){
            this.dirty();
        }
    }
    void setPageStateless(final Boolean stateless){
        this.stateless=stateless;
    }
    public MarkupType getMarkupType(){
        throw new UnsupportedOperationException("Page does not support markup. This error can happen if you have extended Page directly, instead extend WebPage");
    }
    public PageReference getPageReference(){
        this.setStatelessHint(false);
        return new PageReference(this.numericId);
    }
    public int getPageId(){
        return this.numericId;
    }
    public int getRenderCount(){
        return this.renderCount;
    }
    public final void setWasCreatedBookmarkable(final boolean wasCreatedBookmarkable){
        this.setFlag(524288,wasCreatedBookmarkable);
    }
    public final boolean wasCreatedBookmarkable(){
        return this.getFlag(524288);
    }
    public void renderPage(){
        final boolean frozen=this.setFreezePageId(true);
        try{
            ++this.renderCount;
            this.render();
        }
        finally{
            this.setFreezePageId(frozen);
        }
    }
    public final boolean wasRendered(final Component component){
        return this.renderedComponents!=null&&this.renderedComponents.contains(component);
    }
    static{
        log=LoggerFactory.getLogger(Page.class);
    }
}
