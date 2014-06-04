package org.apache.wicket;

import org.apache.wicket.util.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.behavior.*;
import java.io.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.feedback.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.cycle.*;
import java.lang.reflect.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.html.internal.*;
import java.util.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.model.*;
import org.apache.wicket.event.*;
import org.slf4j.*;

public abstract class Component implements IClusterable,IConverterLocator,IRequestableComponent,IHeaderContributor,IHierarchical<Component>,IEventSink,IEventSource{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public static final Action ENABLE;
    public static final char PATH_SEPARATOR=':';
    public static final String PARENT_PATH="..";
    public static final Action RENDER;
    private static final MetaDataKey<String> MARKUP_ID_KEY;
    private static final IModelComparator defaultModelComparator;
    private static final int FLAG_UNUSED0=536870912;
    private static final int FLAG_UNUSED1=8388608;
    private static final int FLAG_UNUSED2=16777216;
    private static final int FLAG_UNUSED3=268435456;
    private static final int FLAG_AUTO=1;
    private static final int FLAG_ESCAPE_MODEL_STRINGS=2;
    static final int FLAG_INHERITABLE_MODEL=4;
    private static final int FLAG_VERSIONED=8;
    private static final int FLAG_VISIBLE=16;
    private static final int FLAG_RENDER_BODY_ONLY=32;
    private static final int FLAG_IGNORE_ATTRIBUTE_MODIFIER=64;
    private static final int FLAG_ENABLED=128;
    protected static final int FLAG_RESERVED1=256;
    protected static final int FLAG_RESERVED2=512;
    protected static final int FLAG_RESERVED3=1024;
    protected static final int FLAG_RESERVED4=2048;
    private static final int FLAG_HAS_BEEN_RENDERED=4096;
    private static final int FLAG_IS_RENDER_ALLOWED=8192;
    private static final int FLAG_OUTPUT_MARKUP_ID=16384;
    private static final int FLAG_PLACEHOLDER=32768;
    protected static final int FLAG_RESERVED5=65536;
    protected static final int FLAG_INITIALIZED=131072;
    private static final int FLAG_NOTUSED7=262144;
    protected static final int FLAG_RESERVED8=524288;
    private static final int FLAG_MODEL_SET=1048576;
    protected static final int FLAG_REMOVING_FROM_HIERARCHY=2097152;
    private static final int FLAG_RENDERING=33554432;
    private static final int FLAG_PREPARED_FOR_RENDER=67108864;
    private static final int FLAG_AFTER_RENDERING=134217728;
    private static final int FLAG_MARKUP_ATTACHED=268435456;
    private static final int FLAG_VISIBILITY_ALLOWED=1073741824;
    private static final int FLAG_DETACHING=Integer.MIN_VALUE;
    private static final String MARKUP_ID_ATTR_NAME="id";
    static final MetaDataKey<String> ADDED_AT_KEY;
    static final MetaDataKey<String> CONSTRUCTED_AT_KEY;
    private int flags;
    private static final short RFLAG_ENABLED_IN_HIERARCHY_VALUE=1;
    private static final short RFLAG_ENABLED_IN_HIERARCHY_SET=2;
    private static final short RFLAG_VISIBLE_IN_HIEARARCHY_VALUE=4;
    private static final short RFLAG_VISIBLE_IN_HIERARCHY_SET=8;
    private static final short RFLAG_CONFIGURED=16;
    private static final short RFLAG_BEFORE_RENDER_SUPER_CALL_VERIFIED=32;
    private static final short RFLAG_INITIALIZE_SUPER_CALL_VERIFIED=64;
    private transient short requestFlags;
    private String id;
    private MarkupContainer parent;
    int generatedMarkupId;
    private transient IMarkupFragment markup;
    private transient IMarkupSourcingStrategy markupSourcingStrategy;
    Object data;
    private static final MetaDataKey<List<Component>> FEEDBACK_LIST;
    final int data_start(){
        return this.getFlag(1048576)?1:0;
    }
    final int data_length(){
        if(this.data==null){
            return 0;
        }
        if(this.data instanceof Object[]&&!(this.data instanceof MetaDataEntry[])){
            return ((Object[])this.data).length;
        }
        return 1;
    }
    final Object data_get(final int index){
        if(this.data==null){
            return null;
        }
        if(this.data instanceof Object[]&&!(this.data instanceof MetaDataEntry[])){
            final Object[] array=(Object[])this.data;
            return (index<array.length)?array[index]:null;
        }
        if(index==0){
            return this.data;
        }
        return null;
    }
    final void data_set(final int index,final Object object){
        if(index>this.data_length()-1){
            throw new IndexOutOfBoundsException("can not set data at "+index+" when data_length() is "+this.data_length());
        }
        if(index==0&&(!(this.data instanceof Object[])||this.data instanceof MetaDataEntry[])){
            this.data=object;
        }
        else{
            final Object[] array=(Object[])this.data;
            array[index]=object;
        }
    }
    final void data_add(final Object object){
        this.data_insert(-1,object);
    }
    final void data_insert(int position,final Object object){
        final int currentLength=this.data_length();
        if(position==-1){
            position=currentLength;
        }
        if(position>currentLength){
            throw new IndexOutOfBoundsException("can not insert data at "+position+" when data_length() is "+currentLength);
        }
        if(currentLength==0){
            this.data=object;
        }
        else if(currentLength==1){
            final Object[] array=new Object[2];
            if(position==0){
                array[0]=object;
                array[1]=this.data;
            }
            else{
                array[0]=this.data;
                array[1]=object;
            }
            this.data=array;
        }
        else{
            final Object[] array=new Object[currentLength+1];
            final Object[] current=(Object[])this.data;
            final int after=currentLength-position;
            if(position>0){
                System.arraycopy(current,0,array,0,position);
            }
            array[position]=object;
            if(after>0){
                System.arraycopy(current,position,array,position+1,after);
            }
            this.data=array;
        }
    }
    final void data_remove(final int position){
        final int currentLength=this.data_length();
        if(position>currentLength-1){
            throw new IndexOutOfBoundsException();
        }
        if(currentLength==1){
            this.data=null;
        }
        else if(currentLength==2){
            final Object[] current=(Object[])this.data;
            if(position==0){
                this.data=current[1];
            }
            else{
                this.data=current[0];
            }
        }
        else{
            final Object[] current=(Object[])this.data;
            this.data=new Object[currentLength-1];
            if(position>0){
                System.arraycopy(current,0,this.data,0,position);
            }
            if(position!=currentLength-1){
                final int left=currentLength-position-1;
                System.arraycopy(current,position+1,this.data,position,left);
            }
        }
    }
    public Component(final String id){
        this(id,null);
    }
    public Component(final String id,final IModel<?> model){
        super();
        this.flags=1073815706;
        this.requestFlags=0;
        this.generatedMarkupId=-1;
        this.data=null;
        this.setId(id);
        this.getApplication().getComponentInstantiationListeners().onInstantiation(this);
        final IDebugSettings debugSettings=this.getApplication().getDebugSettings();
        if(debugSettings.isLinePreciseReportingOnNewComponentEnabled()&&debugSettings.getComponentUseCheck()){
            this.setMetaData(Component.CONSTRUCTED_AT_KEY,ComponentStrings.toString(this,new MarkupException("constructed")));
        }
        if(model!=null){
            this.setModelImpl(this.wrap(model));
        }
    }
    public IMarkupFragment getMarkup(){
        if(this.markup!=null){
            return this.markup;
        }
        if(this.parent==null){
            if(this instanceof MarkupContainer){
                final MarkupContainer container=(MarkupContainer)this;
                final Markup associatedMarkup=container.getAssociatedMarkup();
                if(associatedMarkup!=null){
                    return this.markup=associatedMarkup;
                }
            }
            throw new MarkupNotFoundException("Can not determine Markup. Component is not yet connected to a parent. "+this.toString());
        }
        return this.markup=this.parent.getMarkup(this);
    }
    private boolean internalOnMarkupAttached(){
        final boolean rtn=this.getFlag(268435456);
        if(!rtn){
            this.setFlag(268435456,true);
            this.onMarkupAttached();
        }
        return rtn;
    }
    @Deprecated
    protected void onMarkupAttached(){
        if(Component.log.isDebugEnabled()){
            Component.log.debug("Markup available "+this.toString());
        }
    }
    public final String getMarkupIdFromMarkup(){
        final ComponentTag tag=this.getMarkupTag();
        if(tag!=null){
            final String id=tag.getAttribute("id");
            if(!Strings.isEmpty((CharSequence)id)){
                return id.trim();
            }
        }
        return null;
    }
    public final void setMarkup(final IMarkupFragment markup){
        this.markup=markup;
    }
    protected void onConfigure(){
    }
    protected void onInitialize(){
        this.setRequestFlag((short)64,true);
        try{
            this.internalOnMarkupAttached();
        }
        catch(WicketRuntimeException ex){
        }
    }
    final boolean isInitialized(){
        return this.getFlag(131072);
    }
    public void internalInitialize(){
        this.fireInitialize();
    }
    final void fireInitialize(){
        if(!this.getFlag(131072)){
            this.setFlag(131072,true);
            this.setRequestFlag((short)64,false);
            this.onInitialize();
            if(!this.getRequestFlag((short)64)){
                throw new IllegalStateException(Component.class.getName()+" has not been properly initialized. Something in the hierarchy of "+this.getClass().getName()+" has not called super.onInitialize() in the override of onInitialize() method");
            }
            this.setRequestFlag((short)64,false);
            this.getApplication().getComponentInitializationListeners().onInitialize(this);
        }
    }
    public final void afterRender(){
        try{
            this.setFlag(134217728,true);
            this.onAfterRenderChildren();
            this.onAfterRender();
            this.getApplication().getComponentOnAfterRenderListeners().onAfterRender(this);
            if(this.getFlag(134217728)){
                throw new IllegalStateException(Component.class.getName()+" has not been properly detached. Something in the hierarchy of "+this.getClass().getName()+" has not called super.onAfterRender() in the override of onAfterRender() method");
            }
        }
        finally{
            this.setFlag(33554432,false);
        }
    }
    private final void internalBeforeRender(){
        this.configure();
        if(this.determineVisibility()&&!this.getFlag(33554432)&&!this.getFlag(67108864)){
            this.setRequestFlag((short)32,false);
            this.getApplication().getComponentPreOnBeforeRenderListeners().onBeforeRender(this);
            this.onBeforeRender();
            this.getApplication().getComponentPostOnBeforeRenderListeners().onBeforeRender(this);
            if(!this.getRequestFlag((short)32)){
                throw new IllegalStateException(Component.class.getName()+" has not been properly rendered. Something in the hierarchy of "+this.getClass().getName()+" has not called super.onBeforeRender() in the override of onBeforeRender() method");
            }
        }
    }
    public final void beforeRender(){
        if(!(this instanceof IFeedback)){
            this.internalBeforeRender();
        }
        else{
            List<Component> feedbacks=this.getRequestCycle().getMetaData(Component.FEEDBACK_LIST);
            if(feedbacks==null){
                feedbacks=(List<Component>)new ArrayList();
                this.getRequestCycle().setMetaData(Component.FEEDBACK_LIST,feedbacks);
            }
            if(this instanceof MarkupContainer){
                ((MarkupContainer)this).visitChildren((Class<?>)IFeedback.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                    public void component(final Component component,final IVisit<Void> visit){
                        component.beforeRender();
                    }
                });
            }
            if(!feedbacks.contains(this)){
                feedbacks.add(this);
            }
        }
    }
    public final void configure(){
        if(!this.getRequestFlag((short)16)){
            this.clearEnabledInHierarchyCache();
            this.clearVisibleInHierarchyCache();
            this.onConfigure();
            for(final Behavior behavior : this.getBehaviors()){
                if(this.isBehaviorAccepted(behavior)){
                    behavior.onConfigure(this);
                }
            }
            this.setRenderAllowed();
            this.internalOnAfterConfigure();
            this.setRequestFlag((short)16,true);
        }
    }
    void internalOnAfterConfigure(){
    }
    public final boolean continueToOriginalDestination(){
        return RestartResponseAtInterceptPageException.continueToOriginalDestination();
    }
    public final void debug(final Serializable message){
        this.getSession().getFeedbackMessages().debug(this,message);
        this.getSession().dirty();
    }
    final void internalOnRemove(){
        this.setFlag(2097152,true);
        this.onRemove();
        if(this.getFlag(2097152)){
            throw new IllegalStateException(Component.class.getName()+" has not been properly removed from hierachy. Something in the hierarchy of "+this.getClass().getName()+" has not called super.onRemovalFromHierarchy() in the override of onRemovalFromHierarchy() method");
        }
        this.removeChildren();
    }
    public final void detach(){
        this.setFlag(Integer.MIN_VALUE,true);
        this.onDetach();
        if(this.getFlag(Integer.MIN_VALUE)){
            throw new IllegalStateException(Component.class.getName()+" has not been properly detached. Something in the hierarchy of "+this.getClass().getName()+" has not called super.onDetach() in the override of onDetach() method");
        }
        this.detachModels();
        new Behaviors(this).detach();
        this.detachChildren();
        if(this.getFlag(4)){
            this.setModelImpl(null);
            this.setFlag(4,false);
        }
        this.clearEnabledInHierarchyCache();
        this.clearVisibleInHierarchyCache();
        this.requestFlags=0;
        this.internalDetach();
        final IDetachListener detachListener=this.getApplication().getFrameworkSettings().getDetachListener();
        if(detachListener!=null){
            detachListener.onDetach(this);
        }
    }
    private void internalDetach(){
        this.markup=null;
    }
    public void detachModels(){
        this.detachModel();
    }
    public final void error(final Serializable message){
        this.getSession().getFeedbackMessages().error(this,message);
        this.getSession().dirty();
    }
    public final void fatal(final Serializable message){
        this.getSession().getFeedbackMessages().fatal(this,message);
        this.getSession().dirty();
    }
    public final <Z> Z findParent(final Class<Z> c){
        for(MarkupContainer current=this.parent;current!=null;current=current.getParent()){
            if(c.isInstance(current)){
                return (Z)c.cast((Object)current);
            }
        }
        return null;
    }
    public final MarkupContainer findParentWithAssociatedMarkup(){
        for(MarkupContainer container=this.parent;container!=null;container=container.getParent()){
            if(container.getAssociatedMarkup()!=null){
                return container;
            }
        }
        throw new WicketRuntimeException("Unable to find parent with associated markup");
    }
    public final Application getApplication(){
        return Application.get();
    }
    public final String getClassRelativePath(){
        return this.getClass().getName()+':'+this.getPageRelativePath();
    }
    public <C> IConverter<C> getConverter(final Class<C> type){
        return this.getApplication().getConverterLocator().getConverter(type);
    }
    public final boolean getEscapeModelStrings(){
        return this.getFlag(2);
    }
    public final FeedbackMessage getFeedbackMessage(){
        return this.getSession().getFeedbackMessages().messageForComponent(this);
    }
    public final List<FeedbackMessage> getFeedbackMessages(){
        return this.getSession().getFeedbackMessages().messagesForComponent(this);
    }
    public String getId(){
        return this.id;
    }
    public final IModel<?> getInnermostModel(){
        return this.getInnermostModel(this.getDefaultModel());
    }
    public Locale getLocale(){
        if(this.parent!=null){
            return this.parent.getLocale();
        }
        return this.getSession().getLocale();
    }
    public final Localizer getLocalizer(){
        return this.getApplication().getResourceSettings().getLocalizer();
    }
    private final ComponentTag getMarkupTag(){
        final IMarkupFragment markup=this.getMarkup();
        if(markup!=null){
            for(int i=0;i<markup.size();++i){
                final MarkupElement elem=markup.get(i);
                if(elem instanceof ComponentTag){
                    return (ComponentTag)elem;
                }
            }
        }
        return null;
    }
    public final ValueMap getMarkupAttributes(){
        final ComponentTag tag=this.getMarkupTag();
        if(tag!=null){
            final ValueMap attrs=new ValueMap((Map)tag.getAttributes());
            attrs.makeImmutable();
            return attrs;
        }
        return ValueMap.EMPTY_MAP;
    }
    public final Object getMarkupIdImpl(){
        if(this.generatedMarkupId!=-1){
            return this.generatedMarkupId;
        }
        String id=this.getMetaData(Component.MARKUP_ID_KEY);
        if(id==null&&this.findPage()!=null){
            id=this.getMarkupIdFromMarkup();
        }
        return id;
    }
    private final int nextAutoIndex(){
        final Page page=this.findPage();
        if(page==null){
            throw new WicketRuntimeException("This component is not (yet) coupled to a page. It has to be able to find the page it is supposed to operate in before you can call this method (Component#getMarkupId)");
        }
        return page.getAutoIndex();
    }
    public String getMarkupId(final boolean createIfDoesNotExist){
        final Object storedMarkupId=this.getMarkupIdImpl();
        if(storedMarkupId instanceof String){
            return (String)storedMarkupId;
        }
        if(storedMarkupId==null&&!createIfDoesNotExist){
            return null;
        }
        int generatedMarkupId=(int)((storedMarkupId instanceof Integer)?storedMarkupId:this.getSession().nextSequenceValue());
        if(generatedMarkupId==173){
            generatedMarkupId=this.getSession().nextSequenceValue();
        }
        if(storedMarkupId==null){
            this.setMarkupIdImpl(generatedMarkupId);
        }
        String markupIdPrefix="id";
        if(!this.getApplication().usesDeploymentConfig()){
            markupIdPrefix=this.getId();
        }
        final String markupIdPostfix=Integer.toHexString(generatedMarkupId).toLowerCase();
        String markupId=markupIdPrefix+markupIdPostfix;
        final char c=markupId.charAt(0);
        if(!Character.isLetter(c)){
            markupId="id"+markupId;
        }
        markupId=Strings.replaceAll((CharSequence)markupId,(CharSequence)"_",(CharSequence)"__").toString();
        markupId=markupId.replace('.','_');
        markupId=markupId.replace('-','_');
        markupId=markupId.replace(' ','_');
        return markupId;
    }
    public String getMarkupId(){
        return this.getMarkupId(true);
    }
    public final <M extends Serializable> M getMetaData(final MetaDataKey<M> key){
        return key.get(this.getMetaData());
    }
    private MetaDataEntry<?>[] getMetaData(){
        MetaDataEntry<?>[] metaData=null;
        final int index=this.getFlag(1048576)?1:0;
        final int length=this.data_length();
        if(index<length){
            final Object object=this.data_get(index);
            if(object instanceof MetaDataEntry[]){
                metaData=(MetaDataEntry<?>[])object;
            }
            else if(object instanceof MetaDataEntry){
                metaData=(MetaDataEntry<?>[])new MetaDataEntry[] { (MetaDataEntry)object };
            }
        }
        return metaData;
    }
    public final IModel<?> getDefaultModel(){
        IModel<?> model=this.getModelImpl();
        if(model==null){
            model=this.initModel();
            this.setModelImpl(model);
        }
        return model;
    }
    public final Object getDefaultModelObject(){
        final IModel<?> model=this.getDefaultModel();
        if(model!=null){
            try{
                return model.getObject();
            }
            catch(Exception ex){
                final RuntimeException rex=new RuntimeException("An error occurred while getting the model object for Component: "+this.toString(true),(Throwable)ex);
                throw rex;
            }
        }
        return null;
    }
    public final String getDefaultModelObjectAsString(){
        return this.getDefaultModelObjectAsString(this.getDefaultModelObject());
    }
    public final String getDefaultModelObjectAsString(final Object modelObject){
        if(modelObject!=null){
            final Class<?> objectClass=(Class<?>)modelObject.getClass();
            final IConverter converter=this.getConverter(objectClass);
            final String modelString=converter.convertToString(modelObject,this.getLocale());
            if(modelString!=null){
                if(this.getFlag(2)){
                    return Strings.escapeMarkup((CharSequence)modelString,false,false).toString();
                }
                return modelString;
            }
        }
        return "";
    }
    public final boolean getOutputMarkupId(){
        return this.getFlag(16384);
    }
    public final boolean getOutputMarkupPlaceholderTag(){
        return this.getFlag(32768);
    }
    public final Page getPage(){
        final Page page=this.findPage();
        if(page==null){
            throw new WicketRuntimeException("No Page found for component "+this);
        }
        return page;
    }
    public final String getPageRelativePath(){
        return Strings.afterFirstPathComponent(this.getPath(),':');
    }
    public final MarkupContainer getParent(){
        return this.parent;
    }
    public final String getPath(){
        final PrependingStringBuffer buffer=new PrependingStringBuffer(32);
        for(Component c=this;c!=null;c=c.getParent()){
            if(buffer.length()>0){
                buffer.prepend(':');
            }
            buffer.prepend(c.getId());
        }
        return buffer.toString();
    }
    public final boolean getRenderBodyOnly(){
        return this.getFlag(32);
    }
    public final Request getRequest(){
        final RequestCycle requestCycle=this.getRequestCycle();
        if(requestCycle==null){
            throw new WicketRuntimeException("No RequestCycle is currently set!");
        }
        return requestCycle.getRequest();
    }
    public final RequestCycle getRequestCycle(){
        return RequestCycle.get();
    }
    public final Response getResponse(){
        return this.getRequestCycle().getResponse();
    }
    public Session getSession(){
        return Session.get();
    }
    public long getSizeInBytes(){
        final MarkupContainer originalParent=this.parent;
        this.parent=null;
        long size=-1L;
        try{
            size=WicketObjects.sizeof((Serializable)this);
        }
        catch(Exception e){
            Component.log.error("Exception getting size for component "+this,e);
        }
        this.parent=originalParent;
        return size;
    }
    public final String getString(final String key){
        return this.getString(key,null);
    }
    public final String getString(final String key,final IModel<?> model){
        return this.getLocalizer().getString(key,this,model);
    }
    public final String getString(final String key,final IModel<?> model,final String defaultValue){
        return this.getLocalizer().getString(key,this,model,defaultValue);
    }
    public final String getStyle(){
        final Session session=this.getSession();
        if(session==null){
            throw new WicketRuntimeException("Wicket Session object not available");
        }
        return session.getStyle();
    }
    public String getVariation(){
        if(this.parent!=null){
            return this.parent.getVariation();
        }
        return null;
    }
    public final boolean hasBeenRendered(){
        return this.getFlag(4096);
    }
    public final boolean hasErrorMessage(){
        return this.getSession().getFeedbackMessages().hasErrorMessageFor(this);
    }
    public final boolean hasFeedbackMessage(){
        return this.getSession().getFeedbackMessages().hasMessageFor(this);
    }
    public final void info(final Serializable message){
        this.getSession().getFeedbackMessages().info(this,message);
        this.getSession().dirty();
    }
    public final void success(final Serializable message){
        this.getSession().getFeedbackMessages().success(this,message);
        this.getSession().dirty();
    }
    public final boolean isActionAuthorized(final Action action){
        final IAuthorizationStrategy authorizationStrategy=this.getSession().getAuthorizationStrategy();
        return authorizationStrategy==null||authorizationStrategy.isActionAuthorized(this,action);
    }
    public final boolean isEnableAllowed(){
        return this.isActionAuthorized(Component.ENABLE);
    }
    public boolean isEnabled(){
        return this.getFlag(128);
    }
    public final boolean isRenderAllowed(){
        return this.getFlag(8192);
    }
    public final boolean isStateless(){
        if((!this.isVisibleInHierarchy()||!this.isEnabledInHierarchy())&&!this.canCallListenerInterface(null)){
            return true;
        }
        if(!this.getStatelessHint()){
            return false;
        }
        for(final Behavior behavior : this.getBehaviors()){
            if(!behavior.getStatelessHint(this)){
                return false;
            }
        }
        return true;
    }
    public boolean isVersioned(){
        return this.getFlag(8)&&(this.parent==null||this.parent.isVersioned());
    }
    public boolean isVisible(){
        return this.getFlag(16);
    }
    public final boolean isVisibleInHierarchy(){
        final Component parent=this.getParent();
        return (parent==null||parent.isVisibleInHierarchy())&&this.determineVisibility();
    }
    public final void markRendering(final boolean setRenderingFlag){
        this.internalMarkRendering(setRenderingFlag);
    }
    public final void modelChanged(){
        this.internalOnModelChanged();
        this.onModelChanged();
    }
    public final void modelChanging(){
        this.checkHierarchyChange(this);
        this.onModelChanging();
        final Page page=this.findPage();
        if(page!=null){
            page.componentModelChanging(this);
        }
    }
    public void internalPrepareForRender(final boolean setRenderingFlag){
        this.beforeRender();
        if(setRenderingFlag){
            final List<Component> feedbacks=this.getRequestCycle().getMetaData(Component.FEEDBACK_LIST);
            if(feedbacks!=null){
                final Component[] arr$;
                final Component[] feedbacksCopy=arr$=(Component[])feedbacks.toArray(new Component[feedbacks.size()]);
                for(final Component feedback : arr$){
                    if(feedback.findPage()!=null){
                        feedback.internalBeforeRender();
                    }
                }
            }
            this.getRequestCycle().setMetaData(Component.FEEDBACK_LIST,null);
        }
        this.markRendering(setRenderingFlag);
    }
    public final void prepareForRender(){
        this.internalPrepareForRender(true);
    }
    public final void redirectToInterceptPage(final Page page){
        throw new RestartResponseAtInterceptPageException(page);
    }
    public final void remove(){
        if(this.parent==null){
            throw new IllegalStateException("Cannot remove "+this+" from null parent!");
        }
        this.parent.remove(this);
    }
    public final void render(){
        RuntimeException exception=null;
        try{
            final MarkupContainer parent=this.getParent();
            if(parent==null||!parent.getFlag(33554432)||this.isAuto()){
                this.internalPrepareForRender(true);
            }
            this.internalRender();
        }
        catch(RuntimeException ex){
            exception=ex;
            try{
                this.afterRender();
            }
            catch(RuntimeException ex2){
                if(exception==null){
                    exception=ex2;
                }
            }
        }
        finally{
            try{
                this.afterRender();
            }
            catch(RuntimeException ex3){
                if(exception==null){
                    exception=ex3;
                }
            }
        }
        if(exception!=null){
            throw exception;
        }
    }
    private final void internalRender(){
        final IMarkupFragment markup=this.getMarkup();
        if(markup==null){
            throw new MarkupNotFoundException("Markup not found for Component: "+this.toString());
        }
        final MarkupStream markupStream=new MarkupStream(markup);
        this.markRendering(true);
        final MarkupElement elem=markup.get(0);
        if(elem instanceof ComponentTag){
            ((ComponentTag)elem).onBeforeRender(this,markupStream);
        }
        if(this.determineVisibility()){
            this.setFlag(4096,true);
            if(Component.log.isDebugEnabled()){
                Component.log.debug("Begin render "+this);
            }
            try{
                this.notifyBehaviorsComponentBeforeRender();
                this.onRender();
                this.notifyBehaviorsComponentRendered();
                this.rendered();
            }
            catch(RuntimeException ex){
                this.onException(ex);
            }
            if(Component.log.isDebugEnabled()){
                Component.log.debug("End render "+this);
            }
        }
        else if(elem!=null&&elem instanceof ComponentTag&&this.getFlag(32768)){
            this.renderPlaceholderTag((ComponentTag)elem,this.getResponse());
        }
    }
    private void onException(final RuntimeException ex){
        for(final Behavior behavior : this.getBehaviors()){
            if(this.isBehaviorAccepted(behavior)){
                try{
                    behavior.onException(this,ex);
                }
                catch(Throwable ex2){
                    Component.log.error("Error while cleaning up after exception",ex2);
                }
            }
        }
        throw ex;
    }
    protected void renderPlaceholderTag(final ComponentTag tag,final Response response){
        final String ns=Strings.isEmpty((CharSequence)tag.getNamespace())?null:(tag.getNamespace()+':');
        response.write((CharSequence)"<");
        if(ns!=null){
            response.write((CharSequence)ns);
        }
        response.write((CharSequence)tag.getName());
        response.write((CharSequence)" id=\"");
        response.write((CharSequence)this.getAjaxRegionMarkupId());
        response.write((CharSequence)"\" style=\"display:none\"></");
        if(ns!=null){
            response.write((CharSequence)ns);
        }
        response.write((CharSequence)tag.getName());
        response.write((CharSequence)">");
    }
    public final String getAjaxRegionMarkupId(){
        String markupId=null;
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior instanceof IAjaxRegionMarkupIdProvider&&behavior.isEnabled(this)){
                markupId=((IAjaxRegionMarkupIdProvider)behavior).getAjaxRegionMarkupId(this);
                break;
            }
        }
        if(markupId==null&&this instanceof IAjaxRegionMarkupIdProvider){
            markupId=((IAjaxRegionMarkupIdProvider)this).getAjaxRegionMarkupId(this);
        }
        if(markupId==null){
            markupId=this.getMarkupId();
        }
        return markupId;
    }
    public final void internalRenderComponent(){
        final IMarkupFragment markup=this.getMarkup();
        if(markup==null){
            throw new MarkupException("Markup not found. Component: "+this.toString());
        }
        final MarkupStream markupStream=new MarkupStream(markup);
        final ComponentTag openTag=markupStream.getTag();
        final ComponentTag tag=openTag.mutable();
        this.onComponentTag(tag);
        if(!tag.isOpenClose()&&!tag.isOpen()){
            markupStream.throwMarkupException("Method renderComponent called on bad markup element: "+tag);
        }
        if(tag.isOpenClose()&&openTag.isOpen()){
            markupStream.throwMarkupException("You can not modify a open tag to open-close: "+tag);
        }
        try{
            if(!this.getRenderBodyOnly()){
                this.renderComponentTag(tag);
            }
            markupStream.next();
            if(tag.isOpen()){
                this.getMarkupSourcingStrategy().onComponentTagBody(this,markupStream,tag);
            }
            if(tag.isOpen()){
                if(openTag.isOpen()){
                    this.renderClosingComponentTag(markupStream,tag,this.getRenderBodyOnly());
                }
                else if(!this.getRenderBodyOnly()&&this.needToRenderTag(openTag)){
                    this.getResponse().write(tag.syntheticCloseTagString());
                }
            }
        }
        catch(WicketRuntimeException wre){
            throw wre;
        }
        catch(RuntimeException re){
            throw new WicketRuntimeException("Exception in rendering component: "+this,re);
        }
    }
    private boolean needToRenderTag(final ComponentTag openTag){
        boolean renderTag=openTag!=null&&!(openTag instanceof WicketTag);
        if(!renderTag){
            renderTag=!this.getApplication().getMarkupSettings().getStripWicketTags();
        }
        return renderTag;
    }
    public final void rendered(){
        final Page page=this.findPage();
        if(page!=null){
            page.componentRendered(this);
        }
        else{
            Component.log.error("Component is not connected to a Page. Cannot register the component as being rendered. Component: "+this.toString());
        }
    }
    protected final IMarkupSourcingStrategy getMarkupSourcingStrategy(){
        if(this.markupSourcingStrategy==null){
            this.markupSourcingStrategy=this.newMarkupSourcingStrategy();
            if(this.markupSourcingStrategy==null){
                this.markupSourcingStrategy=DefaultMarkupSourcingStrategy.get();
            }
        }
        return this.markupSourcingStrategy;
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return null;
    }
    public void renderHead(final HtmlHeaderContainer container){
        if(this.isVisibleInHierarchy()&&this.isRenderAllowed()){
            if(Component.log.isDebugEnabled()){
                Component.log.debug("renderHead: "+this.toString(false));
            }
            final IHeaderResponse response=container.getHeaderResponse();
            if(!response.wasRendered(this)){
                this.getMarkupSourcingStrategy().renderHead(this,container);
                this.renderHead(this,response);
                response.markRendered(this);
            }
            for(final Behavior behavior : this.getBehaviors()){
                if(this.isBehaviorAccepted(behavior)&&!response.wasRendered(behavior)){
                    behavior.renderHead(this,response);
                    final List<IClusterable> pair=(List<IClusterable>)Arrays.asList(new IClusterable[] { this,behavior });
                    response.markRendered(pair);
                }
            }
        }
    }
    public Component replaceWith(final Component replacement){
        if(replacement==null){
            throw new IllegalArgumentException("Argument [[replacement]] cannot be null.");
        }
        if(!this.getId().equals(replacement.getId())){
            throw new IllegalArgumentException("Replacement component must have the same id as the component it will replace. Replacement id [["+replacement.getId()+"]], replaced id [["+this.getId()+"]].");
        }
        if(this.parent==null){
            throw new IllegalStateException("This method can only be called on a component that has already been added to its parent.");
        }
        this.parent.replace(replacement);
        return replacement;
    }
    public final boolean sameInnermostModel(final Component component){
        return this.sameInnermostModel(component.getDefaultModel());
    }
    public final boolean sameInnermostModel(final IModel<?> model){
        final IModel<?> thisModel=this.getDefaultModel();
        return thisModel!=null&&model!=null&&this.getInnermostModel(thisModel)==this.getInnermostModel(model);
    }
    public final Component setEnabled(final boolean enabled){
        if(enabled!=this.getFlag(128)){
            if(this.isVersioned()){
                final Page page=this.findPage();
                if(page!=null){
                    this.addStateChange();
                }
            }
            this.setFlag(128,enabled);
            this.onEnabledStateChanged();
        }
        return this;
    }
    void clearEnabledInHierarchyCache(){
        this.setRequestFlag((short)2,false);
    }
    void onEnabledStateChanged(){
        this.clearEnabledInHierarchyCache();
    }
    public final Component setEscapeModelStrings(final boolean escapeMarkup){
        this.setFlag(2,escapeMarkup);
        return this;
    }
    public final void setMarkupIdImpl(final Object markupId){
        if(markupId!=null&&!(markupId instanceof String)&&!(markupId instanceof Integer)){
            throw new IllegalArgumentException("markupId must be String or Integer");
        }
        if(markupId instanceof Integer){
            this.generatedMarkupId=(int)markupId;
            this.setMetaData(Component.MARKUP_ID_KEY,null);
            return;
        }
        this.generatedMarkupId=-1;
        this.setMetaData(Component.MARKUP_ID_KEY,(String)markupId);
        this.setOutputMarkupId(true);
    }
    final void setMarkupId(final Component comp){
        Args.notNull((Object)comp,"comp");
        this.generatedMarkupId=comp.generatedMarkupId;
        this.setMetaData(Component.MARKUP_ID_KEY,(String)comp.getMetaData((MetaDataKey<M>)Component.MARKUP_ID_KEY));
        if(comp.getOutputMarkupId()){
            this.setOutputMarkupId(true);
        }
    }
    public Component setMarkupId(final String markupId){
        Args.notEmpty((CharSequence)markupId,"markupId");
        this.setMarkupIdImpl(markupId);
        return this;
    }
    public final <M> void setMetaData(final MetaDataKey<M> key,final M object){
        final MetaDataEntry<?>[] old=this.getMetaData();
        Object metaData=null;
        final MetaDataEntry<?>[] metaDataArray=key.set(this.getMetaData(),object);
        if(metaDataArray!=null&&metaDataArray.length>0){
            metaData=((metaDataArray.length>1)?metaDataArray:metaDataArray[0]);
        }
        final int index=this.getFlag(1048576)?1:0;
        if(old==null&&metaData!=null){
            this.data_insert(index,metaData);
        }
        else if(old!=null&&metaData!=null){
            this.data_set(index,metaData);
        }
        else if(old!=null&&metaData==null){
            this.data_remove(index);
        }
    }
    public Component setDefaultModel(final IModel<?> model){
        final IModel<?> prevModel=this.getModelImpl();
        if(prevModel!=null){
            prevModel.detach();
        }
        IModel<?> wrappedModel=prevModel;
        if(prevModel instanceof IWrapModel){
            wrappedModel=(IModel<?>)((IWrapModel)prevModel).getWrappedModel();
        }
        if(wrappedModel!=model){
            if(wrappedModel!=null){
                this.addStateChange();
            }
            this.setModelImpl(this.wrap(model));
        }
        this.modelChanged();
        return this;
    }
    IModel<?> getModelImpl(){
        if(this.getFlag(1048576)){
            return (IModel<?>)this.data_get(0);
        }
        return null;
    }
    void setModelImpl(final IModel<?> model){
        if(this.getFlag(1048576)){
            if(model!=null){
                this.data_set(0,model);
                if(this.getFlag(4)&&!(model instanceof IComponentInheritedModel)){
                    this.setFlag(4,false);
                }
            }
            else{
                this.data_remove(0);
                this.setFlag(1048576,false);
            }
        }
        else if(model!=null){
            this.data_insert(0,model);
            this.setFlag(1048576,true);
        }
    }
    public final Component setDefaultModelObject(final Object object){
        final IModel<Object> model=(IModel<Object>)this.getDefaultModel();
        if(model==null){
            throw new IllegalStateException("Attempt to set model object on null model of component: "+this.getPageRelativePath());
        }
        if(!this.isActionAuthorized(Component.ENABLE)){
            throw new UnauthorizedActionException(this,Component.ENABLE);
        }
        if(!this.getModelComparator().compare(this,object)){
            this.modelChanging();
            model.setObject(object);
            this.modelChanged();
        }
        return this;
    }
    public final Component setOutputMarkupId(final boolean output){
        this.setFlag(16384,output);
        return this;
    }
    public final Component setOutputMarkupPlaceholderTag(final boolean outputTag){
        if(outputTag!=this.getFlag(32768)){
            if(outputTag){
                this.setOutputMarkupId(true);
                this.setFlag(32768,true);
            }
            else{
                this.setFlag(32768,false);
            }
        }
        return this;
    }
    public final Component setRenderBodyOnly(final boolean renderTag){
        this.setFlag(32,renderTag);
        return this;
    }
    public final <C extends IRequestablePage> void setResponsePage(final Class<C> cls){
        this.getRequestCycle().setResponsePage(cls,null);
    }
    public final <C extends IRequestablePage> void setResponsePage(final Class<C> cls,final PageParameters parameters){
        this.getRequestCycle().setResponsePage(cls,parameters);
    }
    public final void setResponsePage(final Page page){
        this.getRequestCycle().setResponsePage(page);
    }
    public Component setVersioned(final boolean versioned){
        this.setFlag(8,versioned);
        return this;
    }
    public final Component setVisible(final boolean visible){
        if(visible!=this.getFlag(16)){
            this.addStateChange();
            this.setFlag(16,visible);
            this.onVisibleStateChanged();
        }
        return this;
    }
    void clearVisibleInHierarchyCache(){
        this.setRequestFlag((short)8,false);
    }
    void onVisibleStateChanged(){
        this.clearVisibleInHierarchyCache();
    }
    public String toString(){
        return this.toString(false);
    }
    public String toString(final boolean detailed){
        try{
            final StringBuilder buffer=new StringBuilder();
            buffer.append("[Component id = ").append(this.getId());
            if(detailed){
                final Page page=this.findPage();
                if(page==null){
                    buffer.append(", page = <No Page>, path = ").append(this.getPath()).append('.').append(Classes.simpleName(this.getClass()));
                }
                else{
                    buffer.append(", page = ").append(this.getPage().getClass().getName()).append(", path = ").append(this.getPath()).append('.').append(Classes.simpleName(this.getClass())).append(", isVisible = ").append(this.determineVisibility()).append(", isVersioned = ").append(this.isVersioned());
                }
                if(this.markup!=null){
                    buffer.append(", markup = ").append(new MarkupStream(this.getMarkup()).toString());
                }
            }
            buffer.append(']');
            return buffer.toString();
        }
        catch(Exception e){
            Component.log.warn("Error while building toString()",e);
            return String.format("[Component id = %s <attributes are not available because exception %s was thrown during toString()>]",new Object[] { this.getId(),e.getClass().getName() });
        }
    }
    public final <C extends Page> CharSequence urlFor(final Class<C> pageClass,final PageParameters parameters){
        return this.getRequestCycle().urlFor(pageClass,parameters);
    }
    @Deprecated
    public final CharSequence urlFor(final Behavior behaviour,final RequestListenerInterface listener){
        return this.urlFor(behaviour,listener,null);
    }
    public final CharSequence urlFor(final Behavior behaviour,final RequestListenerInterface listener,final PageParameters parameters){
        final int id=this.getBehaviorId(behaviour);
        final Page page=this.getPage();
        final PageAndComponentProvider provider=new PageAndComponentProvider(page,this,parameters);
        IRequestHandler handler;
        if(page.isPageStateless()){
            handler=(IRequestHandler)new BookmarkableListenerInterfaceRequestHandler(provider,listener,id);
        }
        else{
            handler=(IRequestHandler)new ListenerInterfaceRequestHandler(provider,listener,id);
        }
        return this.getRequestCycle().urlFor(handler);
    }
    public final CharSequence urlFor(final IRequestHandler requestHandler){
        return this.getRequestCycle().urlFor(requestHandler);
    }
    @Deprecated
    public final CharSequence urlFor(final RequestListenerInterface listener){
        return this.urlFor(listener,null);
    }
    public final CharSequence urlFor(final RequestListenerInterface listener,final PageParameters parameters){
        final Page page=this.getPage();
        final PageAndComponentProvider provider=new PageAndComponentProvider(page,this,parameters);
        IRequestHandler handler;
        if(page.isPageStateless()){
            handler=(IRequestHandler)new BookmarkableListenerInterfaceRequestHandler(provider,listener);
        }
        else{
            handler=(IRequestHandler)new ListenerInterfaceRequestHandler(provider,listener);
        }
        return this.getRequestCycle().urlFor(handler);
    }
    public final CharSequence urlFor(final ResourceReference resourceReference,final PageParameters parameters){
        return this.getRequestCycle().urlFor(resourceReference,parameters);
    }
    public final <R> R visitParents(final Class<?> c,final IVisitor<Component,R> visitor){
        Component current=this.getParent();
        final Visit<R> visit=(Visit<R>)new Visit();
        while(current!=null){
            if(c.isInstance(current)){
                visitor.component((Object)current,(IVisit)visit);
                if(visit.isStopped()){
                    return (R)visit.getResult();
                }
            }
            current=current.getParent();
        }
        return null;
    }
    public final void warn(final Serializable message){
        this.getSession().getFeedbackMessages().warn(this,message);
        this.getSession().dirty();
    }
    private void notifyBehaviorsComponentBeforeRender(){
        for(final Behavior behavior : this.getBehaviors()){
            if(this.isBehaviorAccepted(behavior)){
                behavior.beforeRender(this);
            }
        }
    }
    private void notifyBehaviorsComponentRendered(){
        for(final Behavior behavior : this.getBehaviors()){
            if(this.isBehaviorAccepted(behavior)){
                behavior.afterRender(this);
            }
        }
    }
    protected final void addStateChange(){
        this.checkHierarchyChange(this);
        final Page page=this.findPage();
        if(page!=null){
            page.componentStateChanging(this);
        }
    }
    protected final void checkComponentTag(final ComponentTag tag,final String name){
        if(!tag.getName().equalsIgnoreCase(name)){
            final String msg=String.format("Component [%s] (path = [%s]) must be applied to a tag of type [%s], not: %s",new Object[] { this.getId(),this.getPath(),name,tag.toUserDebugString() });
            this.findMarkupStream().throwMarkupException(msg);
        }
    }
    protected final void checkComponentTagAttribute(final ComponentTag tag,final String key,final String value){
        if(key!=null){
            final String tagAttributeValue=tag.getAttributes().getString(key);
            if(tagAttributeValue==null||!value.equalsIgnoreCase(tagAttributeValue)){
                final String msg=String.format("Component [%s] (path = [%s]) must be applied to a tag with [%s] attribute matching [%s], not [%s]",new Object[] { this.getId(),this.getPath(),key,value,tagAttributeValue });
                this.findMarkupStream().throwMarkupException(msg);
            }
        }
    }
    protected void checkHierarchyChange(final Component component){
        if(!component.isAuto()&&this.getFlag(33554432)){
            throw new WicketRuntimeException("Cannot modify component hierarchy after render phase has started (page version cant change then anymore)");
        }
    }
    protected void detachModel(){
        final IModel<?> model=this.getModelImpl();
        if(model!=null){
            model.detach();
        }
        if(model instanceof IWrapModel&&!this.getFlag(4)){
            ((IWrapModel)model).getWrappedModel().detach();
        }
    }
    protected final String exceptionMessage(final String message){
        return message+":\n"+this.toString();
    }
    protected final MarkupStream findMarkupStream(){
        return new MarkupStream(this.getMarkup());
    }
    protected final Page findPage(){
        return (Page)((this instanceof Page)?this:((Page)this.findParent((Class<Page>)Page.class)));
    }
    public <M extends Behavior> List<M> getBehaviors(final Class<M> type){
        return new Behaviors(this).getBehaviors(type);
    }
    protected final boolean getFlag(final int flag){
        return (this.flags&flag)!=0x0;
    }
    protected final boolean getRequestFlag(final short flag){
        return (this.requestFlags&flag)!=0x0;
    }
    protected final IModel<?> getInnermostModel(final IModel<?> model){
        IModel<?> nested;
        IModel<?> next;
        for(nested=model;nested!=null&&nested instanceof IWrapModel;nested=next){
            next=(IModel<?>)((IWrapModel)nested).getWrappedModel();
            if(nested==next){
                throw new WicketRuntimeException("Model for "+nested+" is self-referential");
            }
        }
        return nested;
    }
    public IModelComparator getModelComparator(){
        return Component.defaultModelComparator;
    }
    protected boolean getStatelessHint(){
        return true;
    }
    protected IModel<?> initModel(){
        IModel<?> foundModel=null;
        for(Component current=this.getParent();current!=null;current=current.getParent()){
            IModel<?> model=current.getModelImpl();
            if(model instanceof IWrapModel&&!(model instanceof IComponentInheritedModel)){
                model=(IModel<?>)((IWrapModel)model).getWrappedModel();
            }
            if(model instanceof IComponentInheritedModel){
                foundModel=(IModel<?>)((IComponentInheritedModel)model).wrapOnInheritance(this);
                this.setFlag(4,true);
                break;
            }
        }
        return foundModel;
    }
    protected void internalOnModelChanged(){
    }
    protected boolean isBehaviorAccepted(final Behavior behavior){
        return (!(behavior instanceof AttributeModifier)||!this.getFlag(64))&&behavior.isEnabled(this);
    }
    protected final boolean isIgnoreAttributeModifier(){
        return this.getFlag(64);
    }
    @Deprecated
    protected MarkupStream locateMarkupStream(){
        return new MarkupStream(this.getMarkup());
    }
    protected void onAfterRender(){
        this.setFlag(134217728,false);
    }
    protected void onBeforeRender(){
        this.setFlag(67108864,true);
        this.onBeforeRenderChildren();
        this.setRequestFlag((short)32,true);
    }
    protected void onComponentTag(final ComponentTag tag){
        if(this.getFlag(16384)){
            tag.putInternal("id",(CharSequence)this.getMarkupId());
        }
        if(this.getApplication().getDebugSettings().isOutputComponentPath()){
            String path=this.getPageRelativePath();
            path=path.replace((CharSequence)"_",(CharSequence)"__");
            path=path.replace((CharSequence)":",(CharSequence)"_");
            tag.put("wicketpath",(CharSequence)path);
        }
        this.getMarkupSourcingStrategy().onComponentTag(this,tag);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
    }
    protected void onDetach(){
        this.setFlag(Integer.MIN_VALUE,false);
    }
    protected void onRemove(){
        this.setFlag(2097152,false);
    }
    protected void onModelChanged(){
    }
    protected void onModelChanging(){
    }
    protected abstract void onRender();
    protected final void renderComponentTag(ComponentTag tag){
        if(this.needToRenderTag(tag)){
            if(tag.hasBehaviors()){
                final Iterator<? extends Behavior> tagBehaviors=tag.getBehaviors();
                while(tagBehaviors.hasNext()){
                    final Behavior behavior=(Behavior)tagBehaviors.next();
                    if(behavior.isEnabled(this)){
                        behavior.onComponentTag(this,tag);
                    }
                    behavior.detach(this);
                }
            }
            final List<? extends Behavior> behaviors=this.getBehaviors();
            if(behaviors!=null&&!behaviors.isEmpty()&&!tag.isClose()&&!this.isIgnoreAttributeModifier()){
                tag=tag.mutable();
                for(final Behavior behavior2 : behaviors){
                    if(this.isBehaviorAccepted(behavior2)){
                        behavior2.onComponentTag(this,tag);
                    }
                }
            }
            if(tag instanceof WicketTag&&!tag.isClose()&&!this.getFlag(64)){
                if(this.getFlag(16384)){
                    Component.log.warn(String.format("Markup id set on a component that is usually not rendered into markup. Markup id: %s, component id: %s, component tag: %s.",new Object[] { this.getMarkupId(),this.getId(),tag.getName() }));
                }
                if(this.getFlag(32768)){
                    Component.log.warn(String.format("Placeholder tag set on a component that is usually not rendered into markup. Component id: %s, component tag: %s.",new Object[] { this.getId(),tag.getName() }));
                }
            }
            tag.writeOutput(this.getResponse(),!this.needToRenderTag(null),this.getMarkup().getMarkupResourceStream().getWicketNamespace());
        }
    }
    protected final void replaceComponentTagBody(final MarkupStream markupStream,final ComponentTag tag,final CharSequence body){
        ComponentTag markupOpenTag=null;
        if(tag.isOpen()){
            markupOpenTag=markupStream.getPreviousTag();
            if(markupOpenTag.isOpen()){
                markupStream.skipRawMarkup();
            }
        }
        if(body!=null){
            this.getResponse().write(body);
        }
        if(tag.isOpen()&&markupOpenTag!=null&&markupOpenTag.isOpen()&&!markupStream.atCloseTag()){
            markupStream.throwMarkupException("Expected close tag for '"+markupOpenTag+"' Possible attempt to embed component(s) '"+markupStream.get()+"' in the body of this component which discards its body");
        }
    }
    protected final void setAuto(final boolean auto){
        this.setFlag(1,auto);
    }
    protected final void setFlag(final int flag,final boolean set){
        if(set){
            this.flags|=flag;
        }
        else{
            this.flags&=~flag;
        }
    }
    protected final void setRequestFlag(final short flag,final boolean set){
        if(set){
            this.requestFlags|=flag;
        }
        else{
            this.requestFlags&=(short)~flag;
        }
    }
    protected final Component setIgnoreAttributeModifier(final boolean ignore){
        this.setFlag(64,ignore);
        return this;
    }
    protected final <V> IModel<V> wrap(final IModel<V> model){
        if(model instanceof IComponentAssignedModel){
            return (IModel<V>)((IComponentAssignedModel)model).wrapOnAssignment(this);
        }
        return model;
    }
    void detachChildren(){
    }
    void removeChildren(){
    }
    public Component get(final String path){
        if(path.length()==0){
            return this;
        }
        throw new IllegalArgumentException(this.exceptionMessage("Component is not a container and so does not contain the path "+path));
    }
    final boolean hasMarkupIdMetaData(){
        return this.getMarkupId()!=null;
    }
    void internalMarkRendering(final boolean setRenderingFlag){
        if(setRenderingFlag){
            this.setFlag(67108864,false);
            this.setFlag(33554432,true);
        }
        else{
            this.setFlag(33554432,false);
        }
    }
    public final boolean isAuto(){
        for(Component current=this;current!=null;current=current.getParent()){
            if(current.getFlag(1)){
                return true;
            }
        }
        return false;
    }
    boolean isPreparedForRender(){
        return this.getFlag(67108864);
    }
    protected void onAfterRenderChildren(){
    }
    void onBeforeRenderChildren(){
    }
    final void renderClosingComponentTag(final MarkupStream markupStream,final ComponentTag openTag,final boolean renderBodyOnly){
        if(openTag.isOpen()){
            if(markupStream.atCloseTag()&&markupStream.getTag().closes(openTag)){
                if(!renderBodyOnly&&this.needToRenderTag(openTag)){
                    this.getResponse().write(openTag.syntheticCloseTagString());
                }
            }
            else if(openTag.requiresCloseTag()){
                markupStream.throwMarkupException("Expected close tag for "+openTag);
            }
        }
    }
    final void setId(final String id){
        if(!(this instanceof Page)&&Strings.isEmpty((CharSequence)id)){
            throw new WicketRuntimeException("Null or empty component ID's are not allowed.");
        }
        if(id!=null&&(id.indexOf(58)!=-1||id.indexOf(126)!=-1)){
            throw new WicketRuntimeException("The component ID must not contain ':' or '~' chars.");
        }
        this.id=id;
    }
    public final void setParent(final MarkupContainer parent){
        if(this.parent!=null&&Component.log.isDebugEnabled()){
            Component.log.debug("Replacing parent "+this.parent+" with "+parent);
        }
        this.parent=parent;
    }
    final void setRenderAllowed(final boolean renderAllowed){
        this.setFlag(8192,renderAllowed);
    }
    void setRenderAllowed(){
        this.setRenderAllowed(this.isActionAuthorized(Component.RENDER));
    }
    public final Component setVisibilityAllowed(final boolean allowed){
        this.setFlag(1073741824,allowed);
        return this;
    }
    public final boolean isVisibilityAllowed(){
        return this.getFlag(1073741824);
    }
    public final boolean determineVisibility(){
        return this.isVisible()&&this.isRenderAllowed()&&this.isVisibilityAllowed();
    }
    public final boolean isEnabledInHierarchy(){
        if(this.getRequestFlag((short)2)){
            return this.getRequestFlag((short)1);
        }
        final Component parent=this.getParent();
        final boolean state=(parent==null||parent.isEnabledInHierarchy())&&this.isEnabled()&&this.isEnableAllowed();
        this.setRequestFlag((short)2,true);
        this.setRequestFlag((short)1,state);
        return state;
    }
    @Deprecated
    public final boolean canCallListenerInterface(){
        return true;
    }
    public boolean canCallListenerInterface(final Method method){
        return this.isEnabledInHierarchy()&&this.isVisibleInHierarchy();
    }
    public final void renderHead(final Component component,final IHeaderResponse response){
        if(component!=this){
            throw new IllegalStateException("This method is only meant to be invoked on the component where the parameter component==this");
        }
        this.renderHead(response);
    }
    public void renderHead(final IHeaderResponse response){
    }
    public void onEvent(final IEvent<?> event){
    }
    public final <T> void send(final IEventSink sink,final Broadcast type,final T payload){
        new ComponentEventSender(this,this.getApplication().getFrameworkSettings()).send(sink,type,payload);
    }
    public Component remove(final Behavior... behaviors){
        final Behaviors helper=new Behaviors(this);
        for(final Behavior behavior : behaviors){
            helper.remove(behavior);
        }
        return this;
    }
    public final Behavior getBehaviorById(final int id){
        return new Behaviors(this).getBehaviorById(id);
    }
    public final int getBehaviorId(final Behavior behavior){
        return new Behaviors(this).getBehaviorId(behavior);
    }
    public Component add(final Behavior... behaviors){
        new Behaviors(this).add(behaviors);
        return this;
    }
    public final List<? extends Behavior> getBehaviors(){
        return this.getBehaviors((Class<? extends Behavior>)Behavior.class);
    }
    static{
        log=LoggerFactory.getLogger(Component.class);
        ENABLE=new Action("ENABLE");
        RENDER=new Action("RENDER");
        MARKUP_ID_KEY=new MetaDataKey<String>(){
            private static final long serialVersionUID=1L;
        };
        defaultModelComparator=new IModelComparator(){
            private static final long serialVersionUID=1L;
            public boolean compare(final Component component,final Object b){
                final Object a=component.getDefaultModelObject();
                return (a==null&&b==null)||(a!=null&&b!=null&&a.equals(b));
            }
        };
        ADDED_AT_KEY=new MetaDataKey<String>(){
            private static final long serialVersionUID=1L;
        };
        CONSTRUCTED_AT_KEY=new MetaDataKey<String>(){
            private static final long serialVersionUID=1L;
        };
        FEEDBACK_LIST=new MetaDataKey<List<Component>>(){
            private static final long serialVersionUID=1L;
        };
    }
}
