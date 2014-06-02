package org.apache.wicket;

import org.apache.wicket.markup.html.border.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.iterator.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.request.component.*;
import org.slf4j.*;
import java.util.*;

public abstract class MarkupContainer extends Component implements Iterable<Component>{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private Object children;
    public MarkupContainer(final String id){
        this(id,null);
    }
    public MarkupContainer(final String id,final IModel<?> model){
        super(id,model);
    }
    public MarkupContainer add(final Component... childs){
        for(final Component child : childs){
            Args.notNull((Object)child,"child");
            if(this==child){
                throw new IllegalArgumentException(this.exceptionMessage("Trying to add this component to itself."));
            }
            for(MarkupContainer parent=this.getParent();parent!=null;parent=parent.getParent()){
                if(child==parent){
                    String msg="You can not add a component's parent as child to the component (loop): Component: "+this.toString(false)+"; parent == child: "+parent.toString(false);
                    if(child instanceof Border.BorderBodyContainer){
                        msg=msg+". Please consider using Border.addToBorder(new "+this.getClass().getSimpleName()+"(\""+this.getId()+"\", ...) instead of add(...)";
                    }
                    throw new WicketRuntimeException(msg);
                }
            }
            this.checkHierarchyChange(child);
            if(MarkupContainer.log.isDebugEnabled()){
                MarkupContainer.log.debug("Add "+child.getId()+" to "+this);
            }
            this.addedComponent(child);
            if(this.put(child)!=null){
                throw new IllegalArgumentException(this.exceptionMessage("A child with id '"+child.getId()+"' already exists"));
            }
        }
        return this;
    }
    public MarkupContainer addOrReplace(final Component... childs){
        for(final Component child : childs){
            if(child==null){
                throw new IllegalArgumentException("argument child must be not null");
            }
            this.checkHierarchyChange(child);
            if(this.get(child.getId())==null){
                this.add(child);
            }
            else{
                this.replace(child);
            }
        }
        return this;
    }
    public final boolean autoAdd(final Component component,final MarkupStream markupStream){
        if(component==null){
            throw new IllegalArgumentException("argument component may not be null");
        }
        component.setAuto(true);
        if(markupStream!=null){
            component.setMarkup(markupStream.getMarkupFragment());
        }
        final int index=this.children_indexOf(component);
        if(index>=0){
            this.children_remove(index);
        }
        this.add(component);
        return true;
    }
    public boolean contains(final Component component,final boolean recurse){
        if(component==null){
            throw new IllegalArgumentException("argument component may not be null");
        }
        if(recurse){
            MarkupContainer parent;
            for(Component current=component;current!=null;current=parent){
                parent=current.getParent();
                if(parent==this){
                    return true;
                }
            }
            return false;
        }
        return component.getParent()==this;
    }
    public final Component get(String path){
        if(Strings.isEmpty((CharSequence)path)){
            return this;
        }
        MarkupContainer container=this;
        String id;
        for(id=Strings.firstPathComponent(path,':');"..".equals(id);id=Strings.firstPathComponent(path,':')){
            container=container.getParent();
            if(container==null){
                return null;
            }
            path=((path.length()==id.length())?"":path.substring(id.length()+1));
        }
        if(Strings.isEmpty((CharSequence)id)){
            return container;
        }
        final Component child=container.children_get(id);
        if(child!=null){
            final String path2=Strings.afterFirstPathComponent(path,':');
            return child.get(path2);
        }
        return null;
    }
    public MarkupStream getAssociatedMarkupStream(final boolean throwException){
        final IMarkupFragment markup=this.getAssociatedMarkup();
        if(markup!=null){
            return new MarkupStream(markup);
        }
        if(throwException){
            throw new MarkupNotFoundException("Markup of type '"+this.getMarkupType().getExtension()+"' for component '"+this.getClass().getName()+"' not found."+" Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried.: "+this.toString());
        }
        return null;
    }
    public Markup getAssociatedMarkup(){
        try{
            final Markup markup=MarkupFactory.get().getMarkup(this,false);
            if(markup!=null&&markup!=Markup.NO_MARKUP){
                return markup;
            }
            return null;
        }
        catch(MarkupException ex){
            throw ex;
        }
        catch(MarkupNotFoundException ex2){
            throw ex2;
        }
        catch(WicketRuntimeException ex3){
            throw new MarkupNotFoundException(this.exceptionMessage("Markup of type '"+this.getMarkupType().getExtension()+"' for component '"+this.getClass().getName()+"' not found."+" Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried"),ex3);
        }
    }
    public IMarkupFragment getMarkup(final Component child){
        return this.getMarkupSourcingStrategy().getMarkup(this,child);
    }
    public MarkupType getMarkupType(){
        final MarkupContainer parent=this.getParent();
        if(parent!=null){
            return parent.getMarkupType();
        }
        return null;
    }
    public void internalAdd(final Component child){
        if(MarkupContainer.log.isDebugEnabled()){
            MarkupContainer.log.debug("internalAdd "+child.getId()+" to "+this);
        }
        this.addedComponent(child);
        this.put(child);
    }
    public Iterator<Component> iterator(){
        return new Iterator<Component>(){
            int index=0;
            public boolean hasNext(){
                return this.index<MarkupContainer.this.children_size();
            }
            public Component next(){
                return MarkupContainer.this.children_get(this.index++);
            }
            public void remove(){
                final MarkupContainer this$0=MarkupContainer.this;
                final int n=this.index-1;
                this.index=n;
                final Component removed=this$0.children_remove(n);
                MarkupContainer.this.checkHierarchyChange(removed);
                MarkupContainer.this.removedComponent(removed);
            }
        };
    }
    public final Iterator<Component> iterator(final Comparator<Component> comparator){
        List<Component> sorted;
        if(this.children==null){
            sorted=(List<Component>)Collections.emptyList();
        }
        else if(this.children instanceof Component){
            sorted=(List<Component>)new ArrayList(1);
            sorted.add(this.children);
        }
        else{
            final int size=this.children_size();
            sorted=(List<Component>)new ArrayList(size);
            for(int i=0;i<size;++i){
                sorted.add(this.children_get(i));
            }
        }
        Collections.sort(sorted,comparator);
        return (Iterator<Component>)sorted.iterator();
    }
    public MarkupContainer remove(final Component component){
        this.checkHierarchyChange(component);
        if(component==null){
            throw new IllegalArgumentException("argument component may not be null");
        }
        this.children_remove(component);
        this.removedComponent(component);
        return this;
    }
    public MarkupContainer remove(final String id){
        if(id==null){
            throw new IllegalArgumentException("argument id may not be null");
        }
        final Component component=this.get(id);
        if(component!=null){
            this.remove(component);
            return this;
        }
        throw new WicketRuntimeException("Unable to find a component with id '"+id+"' to remove");
    }
    public MarkupContainer removeAll(){
        if(this.children!=null){
            this.addStateChange();
            for(int size=this.children_size(),i=0;i<size;++i){
                final Object childObject=this.children_get(i,false);
                if(childObject instanceof Component){
                    final Component child=(Component)childObject;
                    child.internalOnRemove();
                    child.detach();
                    child.setParent(null);
                }
            }
            this.children=null;
        }
        return this;
    }
    public final void renderAssociatedMarkup(final String openTagName,final String exceptionMessage){
        final MarkupStream associatedMarkupStream=new MarkupStream(this.getMarkup(null));
        final MarkupElement elem=associatedMarkupStream.get();
        if(!(elem instanceof ComponentTag)){
            associatedMarkupStream.throwMarkupException("Expected the open tag. "+exceptionMessage);
        }
        final ComponentTag associatedMarkupOpenTag=(ComponentTag)elem;
        if(associatedMarkupOpenTag==null||!associatedMarkupOpenTag.isOpen()||!(associatedMarkupOpenTag instanceof WicketTag)){
            associatedMarkupStream.throwMarkupException(exceptionMessage);
        }
        try{
            this.setIgnoreAttributeModifier(true);
            this.renderComponentTag(associatedMarkupOpenTag);
            associatedMarkupStream.next();
            String className=null;
            final boolean outputClassName=this.getApplication().getDebugSettings().isOutputMarkupContainerClassName();
            if(outputClassName){
                className=Classes.name(this.getClass());
                this.getResponse().write((CharSequence)"<!-- MARKUP FOR ");
                this.getResponse().write((CharSequence)className);
                this.getResponse().write((CharSequence)" BEGIN -->");
            }
            this.renderComponentTagBody(associatedMarkupStream,associatedMarkupOpenTag);
            if(outputClassName){
                this.getResponse().write((CharSequence)"<!-- MARKUP FOR ");
                this.getResponse().write((CharSequence)className);
                this.getResponse().write((CharSequence)" END -->");
            }
            this.renderClosingComponentTag(associatedMarkupStream,associatedMarkupOpenTag,false);
        }
        finally{
            this.setIgnoreAttributeModifier(false);
        }
    }
    public MarkupContainer replace(final Component child){
        this.checkHierarchyChange(child);
        if(child==null){
            throw new IllegalArgumentException("argument child must be not null");
        }
        if(MarkupContainer.log.isDebugEnabled()){
            MarkupContainer.log.debug("Replacing "+child.getId()+" in "+this);
        }
        if(child.getParent()!=this){
            final Component replaced=this.put(child);
            if(replaced==null){
                throw new WicketRuntimeException(this.exceptionMessage("Cannot replace a component which has not been added: id='"+child.getId()+"', component="+child));
            }
            this.removedComponent(replaced);
            child.setMarkupId(replaced);
            this.addedComponent(child);
        }
        return this;
    }
    public MarkupContainer setDefaultModel(final IModel<?> model){
        final IModel<?> previous=this.getModelImpl();
        super.setDefaultModel(model);
        if(previous instanceof IComponentInheritedModel){
            this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    IModel<?> compModel=component.getDefaultModel();
                    if(compModel instanceof IWrapModel){
                        compModel=(IModel<?>)((IWrapModel)compModel).getWrappedModel();
                    }
                    if(compModel==previous){
                        component.setDefaultModel(null);
                    }
                    else if(compModel==model){
                        component.modelChanged();
                    }
                }
            });
        }
        return this;
    }
    public int size(){
        return this.children_size();
    }
    public String toString(){
        return this.toString(false);
    }
    public String toString(final boolean detailed){
        final StringBuilder buffer=new StringBuilder();
        buffer.append('[').append(this.getClass().getSimpleName()).append(' ');
        buffer.append(super.toString(detailed));
        if(detailed&&this.children_size()!=0){
            buffer.append(", children = ");
            for(int size=this.children_size(),i=0;i<size;++i){
                final Component child=this.children_get(i);
                if(i!=0){
                    buffer.append(' ');
                }
                buffer.append(child.toString());
            }
        }
        buffer.append(']');
        return buffer.toString();
    }
    public final <S extends Component,R> R visitChildren(final Class<?> clazz,final IVisitor<S,R> visitor){
        return (R)Visits.visitChildren((Iterable)this,(IVisitor)visitor,(IVisitFilter)new ClassVisitFilter((Class)clazz));
    }
    public final <R> R visitChildren(final IVisitor<Component,R> visitor){
        return (R)Visits.visitChildren((Iterable)this,(IVisitor)visitor);
    }
    public final ComponentHierarchyIterator visitChildren(){
        return new ComponentHierarchyIterator(this);
    }
    public final ComponentHierarchyIterator visitChildren(final Class<?> clazz){
        return new ComponentHierarchyIterator(this).filterByClass(clazz);
    }
    private final void addedComponent(final Component child){
        Args.notNull((Object)child,"child");
        final MarkupContainer parent=child.getParent();
        if(parent!=null){
            parent.remove(child);
        }
        child.setParent(this);
        final IDebugSettings debugSettings=Application.get().getDebugSettings();
        if(debugSettings.isLinePreciseReportingOnAddComponentEnabled()&&debugSettings.getComponentUseCheck()){
            child.setMetaData(MarkupContainer.ADDED_AT_KEY,ComponentStrings.toString(child,new MarkupException("added")));
        }
        final Page page=this.findPage();
        if(page!=null){
            page.componentAdded(child);
            if(page.isInitialized()){
                child.internalInitialize();
            }
        }
        if(this.isPreparedForRender()){
            child.beforeRender();
        }
    }
    public final void internalInitialize(){
        super.fireInitialize();
        this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                component.fireInitialize();
            }
        });
    }
    private final void children_add(final Component child){
        if(this.children==null){
            this.children=child;
        }
        else{
            if(!(this.children instanceof ChildList)){
                this.children=new ChildList(this.children);
            }
            ((ChildList)this.children).add(child);
        }
    }
    public final Component get(final int index){
        return this.children_get(index);
    }
    private final Component children_get(final int index){
        return (Component)this.children_get(index,true);
    }
    private final Object postprocess(Object object,final boolean reconstruct,final MarkupContainer parent,final int index){
        if(reconstruct&&object instanceof ComponentSourceEntry){
            object=((ComponentSourceEntry)object).reconstruct(parent,index);
        }
        return object;
    }
    private final Object children_get(final int index,final boolean reconstruct){
        Object component=null;
        if(this.children!=null){
            if(!(this.children instanceof Object[])&&!(this.children instanceof ChildList)){
                if(index!=0){
                    throw new ArrayIndexOutOfBoundsException("index "+index+" is greater then 0");
                }
                component=this.postprocess(this.children,reconstruct,this,0);
                if(this.children!=component){
                    this.children=component;
                }
            }
            else{
                Object[] children;
                if(this.children instanceof ChildList){
                    children=((ChildList)this.children).childs;
                }
                else{
                    children=(Object[])this.children;
                }
                component=this.postprocess(children[index],reconstruct,this,index);
                if(children[index]!=component){
                    children[index]=component;
                }
            }
        }
        return component;
    }
    private final String getId(final Object object){
        if(object instanceof Component){
            return ((Component)object).getId();
        }
        if(object instanceof ComponentSourceEntry){
            return ((ComponentSourceEntry)object).id;
        }
        throw new IllegalArgumentException("Unknown type of object "+object);
    }
    private final Component children_get(final String id){
        if(this.children==null){
            return null;
        }
        Component component=null;
        if(!(this.children instanceof Object[])&&!(this.children instanceof List)){
            if(this.getId(this.children).equals(id)){
                component=(Component)this.postprocess(this.children,true,this,0);
                if(this.children!=component){
                    this.children=component;
                }
            }
        }
        else{
            int size=0;
            Object[] children;
            if(this.children instanceof ChildList){
                children=((ChildList)this.children).childs;
                size=((ChildList)this.children).size;
            }
            else{
                children=(Object[])this.children;
                size=children.length;
            }
            int i=0;
            while(i<size){
                if(this.getId(children[i]).equals(id)){
                    component=(Component)this.postprocess(children[i],true,this,i);
                    if(children[i]!=component){
                        children[i]=component;
                        break;
                    }
                    break;
                }
                else{
                    ++i;
                }
            }
        }
        return component;
    }
    private final int children_indexOf(final Component child){
        if(this.children==null){
            return -1;
        }
        if(!(this.children instanceof Object[])&&!(this.children instanceof ChildList)){
            if(this.getId(this.children).equals(child.getId())){
                return 0;
            }
        }
        else{
            int size=0;
            Object[] children;
            if(this.children instanceof Object[]){
                children=(Object[])this.children;
                size=children.length;
            }
            else{
                children=((ChildList)this.children).childs;
                size=((ChildList)this.children).size;
            }
            for(int i=0;i<size;++i){
                if(this.getId(children[i]).equals(child.getId())){
                    return i;
                }
            }
        }
        return -1;
    }
    private final Component children_remove(final Component component){
        final int index=this.children_indexOf(component);
        if(index!=-1){
            return this.children_remove(index);
        }
        return null;
    }
    private final Component children_remove(final int index){
        if(this.children==null){
            return null;
        }
        if(!(this.children instanceof Component)&&!(this.children instanceof ComponentSourceEntry)){
            if(this.children instanceof Object[]){
                final Object[] c=(Object[])this.children;
                final Object removed=c[index];
                if(c.length==2){
                    if(index==0){
                        this.children=c[1];
                    }
                    else{
                        if(index!=1){
                            throw new IndexOutOfBoundsException();
                        }
                        this.children=c[0];
                    }
                    return (Component)this.postprocess(removed,true,null,-1);
                }
                this.children=new ChildList(this.children);
            }
            final ChildList lst=(ChildList)this.children;
            final Object removed=lst.remove(index);
            if(lst.size==1){
                this.children=lst.get(0);
            }
            return (Component)this.postprocess(removed,true,null,-1);
        }
        if(index==0){
            final Component removed2=(Component)this.postprocess(this.children,true,null,-1);
            this.children=null;
            return removed2;
        }
        throw new IndexOutOfBoundsException();
    }
    private final Object children_set(final int index,final Object child,final boolean reconstruct){
        if(index>=0&&index<this.children_size()){
            Object replaced;
            if(this.children instanceof Component||this.children instanceof ComponentSourceEntry){
                replaced=this.children;
                this.children=child;
            }
            else if(this.children instanceof ChildList){
                replaced=((ChildList)this.children).set(index,child);
            }
            else{
                final Object[] children=(Object[])this.children;
                replaced=children[index];
                children[index]=child;
            }
            return this.postprocess(replaced,reconstruct,null,-1);
        }
        throw new IndexOutOfBoundsException();
    }
    private final Component children_set(final int index,final Component child){
        return (Component)this.children_set(index,child,true);
    }
    private final int children_size(){
        if(this.children==null){
            return 0;
        }
        if(this.children instanceof Component||this.children instanceof ComponentSourceEntry){
            return 1;
        }
        if(this.children instanceof ChildList){
            return ((ChildList)this.children).size;
        }
        return ((Object[])this.children).length;
    }
    private final Component put(final Component child){
        final int index=this.children_indexOf(child);
        if(index==-1){
            this.children_add(child);
            return null;
        }
        return this.children_set(index,child);
    }
    private final void removedComponent(final Component component){
        final Page page=component.findPage();
        if(page!=null){
            page.componentRemoved(component);
        }
        component.detach();
        component.internalOnRemove();
        component.setParent(null);
    }
    protected final boolean renderNext(final MarkupStream markupStream){
        final MarkupElement element=markupStream.get();
        if(element instanceof ComponentTag&&!markupStream.atCloseTag()){
            final ComponentTag tag=(ComponentTag)element;
            final String id=tag.getId();
            Component component=this.get(id);
            if(component==null){
                component=ComponentResolvers.resolve(this,markupStream,tag,null);
                if(component!=null&&component.getParent()==null){
                    this.autoAdd(component,markupStream);
                }
                else if(component!=null){
                    component.setMarkup(markupStream.getMarkupFragment());
                }
            }
            if(component!=null){
                component.render();
            }
            else{
                if(tag.getFlag(32)){
                    this.getResponse().write(element.toCharSequence());
                    return true;
                }
                if(tag instanceof WicketTag){
                    if(((WicketTag)tag).isChildTag()){
                        markupStream.throwMarkupException("Found "+tag.toString()+" but no <wicket:extend>");
                    }
                    else{
                        markupStream.throwMarkupException("Failed to handle: "+tag.toString()+". It might be that no resolver has been registered to handle this special tag. "+" But it also be that you declared wicket:id="+id+" in your markup, but that you either did not add the "+"component to your page at all, or that the hierarchy does not match.");
                    }
                }
                final List<String> names=this.findSimilarComponents(id);
                final StringBuffer msg=new StringBuffer(500);
                msg.append("Unable to find component with id '");
                msg.append(id);
                msg.append("' in ");
                msg.append(this.toString());
                msg.append("\n\tExpected: '");
                msg.append(this.getPageRelativePath());
                msg.append(".");
                msg.append(id);
                msg.append("'.\n\tFound with similar names: '");
                msg.append(Strings.join("', ",(List)names));
                msg.append("'");
                MarkupContainer.log.error(msg.toString());
                markupStream.throwMarkupException(msg.toString());
            }
            return false;
        }
        this.getResponse().write(element.toCharSequence());
        return true;
    }
    private List<String> findSimilarComponents(final String id){
        final List<String> names=(List<String>)Generics.newArrayList();
        final Page page=this.findPage();
        if(page!=null){
            page.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    if(Strings.getLevenshteinDistance((CharSequence)id.toLowerCase(),(CharSequence)component.getId().toLowerCase())<3){
                        names.add(component.getPageRelativePath());
                    }
                }
            });
        }
        return names;
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        this.renderComponentTagBody(markupStream,openTag);
    }
    protected void onRender(){
        this.internalRenderComponent();
    }
    private final void renderComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        if(markupStream!=null&&markupStream.getCurrentIndex()>0){
            final ComponentTag origOpenTag=(ComponentTag)markupStream.get(markupStream.getCurrentIndex()-1);
            if(origOpenTag.isOpenClose()){
                return;
            }
        }
        boolean render=openTag.requiresCloseTag();
        if(!render){
            render=!openTag.hasNoCloseTag();
        }
        if(render){
            this.renderAll(markupStream,openTag);
        }
    }
    protected final void renderAll(final MarkupStream markupStream,final ComponentTag openTag){
        while(markupStream.hasMore()&&(openTag==null||!markupStream.get().closes(openTag))){
            final int index=markupStream.getCurrentIndex();
            final boolean rawMarkup=this.renderNext(markupStream);
            markupStream.setCurrentIndex(index);
            final MarkupElement elem=markupStream.get();
            if(rawMarkup){
                markupStream.next();
            }
            else{
                if(markupStream.getTag().isClose()){
                    throw new WicketRuntimeException("Ups. This should never happen. "+markupStream.toString());
                }
                markupStream.skipComponent();
            }
        }
    }
    void removeChildren(){
        super.removeChildren();
        int i=this.children_size();
        while(i-->0){
            final Object child=this.children_get(i,false);
            if(child instanceof Component){
                final Component component=(Component)child;
                component.internalOnRemove();
            }
        }
    }
    void detachChildren(){
        super.detachChildren();
        int i=this.children_size();
        while(i-->0){
            final Object child=this.children_get(i,false);
            if(child instanceof Component){
                final Component component=(Component)child;
                component.detach();
                if(!component.isAuto()||component instanceof InlineEnclosure){
                    continue;
                }
                this.children_remove(i);
            }
        }
        if(this.children instanceof ChildList){
            final ChildList lst=(ChildList)this.children;
            final Object[] tmp=new Object[lst.size];
            System.arraycopy(lst.childs,0,tmp,0,lst.size);
            this.children=tmp;
        }
    }
    void internalMarkRendering(final boolean setRenderingFlag){
        super.internalMarkRendering(setRenderingFlag);
        for(int size=this.children_size(),i=0;i<size;++i){
            final Component child=this.children_get(i);
            child.internalMarkRendering(setRenderingFlag);
        }
    }
    private Component[] copyChildren(){
        final int size=this.children_size();
        final Component[] result=new Component[size];
        for(int i=0;i<size;++i){
            result[i]=this.children_get(i);
        }
        return result;
    }
    void onBeforeRenderChildren(){
        super.onBeforeRenderChildren();
        final Component[] children=this.copyChildren();
        try{
            for(final Component child : children){
                if(child.getParent()==this){
                    child.beforeRender();
                }
            }
        }
        catch(RuntimeException ex){
            if(ex instanceof WicketRuntimeException){
                throw ex;
            }
            throw new WicketRuntimeException("Error attaching this container for rendering: "+this,ex);
        }
    }
    void onEnabledStateChanged(){
        super.onEnabledStateChanged();
        this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                component.clearEnabledInHierarchyCache();
            }
        });
    }
    void onVisibleStateChanged(){
        super.onVisibleStateChanged();
        this.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                component.clearVisibleInHierarchyCache();
            }
        });
    }
    protected void onAfterRenderChildren(){
        for(final Component child : this){
            child.markRendering(false);
        }
        super.onAfterRenderChildren();
    }
    @Deprecated
    public boolean hasAssociatedMarkup(){
        return this.getAssociatedMarkup()!=null;
    }
    public final void swap(final int idx1,final int idx2){
        final int size=this.children_size();
        if(idx1<0||idx1>=size){
            throw new IndexOutOfBoundsException("Argument idx is out of bounds: "+idx1+"<>[0,"+size+")");
        }
        if(idx2<0||idx2>=size){
            throw new IndexOutOfBoundsException("Argument idx is out of bounds: "+idx2+"<>[0,"+size+")");
        }
        if(idx1==idx2){
            return;
        }
        if(this.children instanceof Object[]){
            final Object[] array=(Object[])this.children;
            final Object tmp=array[idx1];
            array[idx1]=array[idx2];
            array[idx2]=tmp;
        }
        else{
            final ChildList list=(ChildList)this.children;
            final Object tmp=list.childs[idx1];
            list.childs[idx1]=list.childs[idx2];
            list.childs[idx2]=tmp;
        }
    }
    protected void onMarkupAttached(){
        super.onMarkupAttached();
    }
    private void createAndAddComponentsForWicketTags(){
        final IMarkupFragment markup=this.getMarkup();
        if(markup!=null&&markup.size()>1){
            final MarkupStream stream=new MarkupStream(markup);
            if(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)){
                stream.next();
            }
            while(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)){
                final ComponentTag tag=stream.getTag();
                if(tag.isOpen()||tag.isOpenClose()){
                    if(tag instanceof WicketTag){
                        final Component component=ComponentResolvers.resolve(this,stream,tag,null);
                        if(component!=null&&component.getParent()==null){
                            if(!component.getId().equals(tag.getId())){
                                tag.setId(component.getId());
                                tag.setModified(true);
                            }
                            this.add(component);
                        }
                    }
                    if(tag.isOpen()){
                        stream.skipToMatchingCloseTag(tag);
                    }
                }
                stream.next();
            }
        }
    }
    static{
        log=LoggerFactory.getLogger(MarkupContainer.class);
    }
    private static class ComponentSourceEntry extends org.apache.wicket.ComponentSourceEntry{
        private static final long serialVersionUID=1L;
        private ComponentSourceEntry(final MarkupContainer container,final Component component,final IComponentSource componentSource){
            super(container,component,componentSource);
        }
        protected void setChild(final MarkupContainer parent,final int index,final Component child){
            parent.children_set(index,child,false);
        }
    }
    private static class ChildList extends AbstractList<Object> implements IClusterable{
        private static final long serialVersionUID=-7861580911447631127L;
        private int size;
        private Object[] childs;
        public ChildList(final Object children){
            super();
            if(children instanceof Object[]){
                this.childs=(Object[])children;
                this.size=this.childs.length;
            }
            else{
                this.childs=new Object[3];
                this.add(children);
            }
        }
        public Object get(final int index){
            return this.childs[index];
        }
        public int size(){
            return this.size;
        }
        public boolean add(final Object o){
            this.ensureCapacity(this.size+1);
            this.childs[this.size++]=o;
            return true;
        }
        public void add(final int index,final Object element){
            if(index>this.size||index<0){
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            this.ensureCapacity(this.size+1);
            System.arraycopy(this.childs,index,this.childs,index+1,this.size-index);
            this.childs[index]=element;
            ++this.size;
        }
        public Object set(final int index,final Object element){
            if(index>=this.size){
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            final Object oldValue=this.childs[index];
            this.childs[index]=element;
            return oldValue;
        }
        public Object remove(final int index){
            if(index>=this.size){
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            final Object oldValue=this.childs[index];
            final int numMoved=this.size-index-1;
            if(numMoved>0){
                System.arraycopy(this.childs,index+1,this.childs,index,numMoved);
            }
            this.childs[--this.size]=null;
            return oldValue;
        }
        public void ensureCapacity(final int minCapacity){
            final int oldCapacity=this.childs.length;
            if(minCapacity>oldCapacity){
                final Object[] oldData=this.childs;
                int newCapacity=oldCapacity*2;
                if(newCapacity<minCapacity){
                    newCapacity=minCapacity;
                }
                System.arraycopy(oldData,0,this.childs=new Object[newCapacity],0,this.size);
            }
        }
    }
}
