package org.apache.wicket;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.request.*;
import org.apache.wicket.authorization.*;
import java.lang.reflect.*;
import java.util.*;
import org.slf4j.*;

public class RequestListenerInterface{
    private static final Map<String,RequestListenerInterface> interfaces;
    private static final Logger log;
    private Method method;
    private final String name;
    private boolean includeRenderCount;
    private boolean renderPageAfterInvocation;
    final Class<? extends IRequestListener> listenerInterfaceClass;
    public static final RequestListenerInterface forName(final String interfaceName){
        return (RequestListenerInterface)RequestListenerInterface.interfaces.get(interfaceName);
    }
    public RequestListenerInterface(final Class<? extends IRequestListener> listenerInterfaceClass){
        super();
        this.includeRenderCount=true;
        this.renderPageAfterInvocation=true;
        this.listenerInterfaceClass=listenerInterfaceClass;
        if(!IRequestListener.class.isAssignableFrom(listenerInterfaceClass)){
            throw new IllegalArgumentException("Class "+listenerInterfaceClass+" must extend IRequestListener");
        }
        final Method[] methods=listenerInterfaceClass.getMethods();
        if(methods.length!=1){
            throw new IllegalArgumentException("Interface "+listenerInterfaceClass+" can have only one method");
        }
        if(methods[0].getParameterTypes().length==0){
            this.method=methods[0];
            this.name=Classes.simpleName((Class)listenerInterfaceClass);
            this.register();
            return;
        }
        throw new IllegalArgumentException("Method "+methods[0]+" in interface "+listenerInterfaceClass+" cannot take any arguments");
    }
    public Class<? extends IRequestListener> getListenerInterfaceClass(){
        return this.listenerInterfaceClass;
    }
    public RequestListenerInterface setIncludeRenderCount(final boolean includeRenderCount){
        this.includeRenderCount=includeRenderCount;
        return this;
    }
    public boolean isIncludeRenderCount(){
        return this.includeRenderCount;
    }
    public RequestListenerInterface setRenderPageAfterInvocation(final boolean renderPageAfterInvocation){
        this.renderPageAfterInvocation=renderPageAfterInvocation;
        return this;
    }
    public boolean isRenderPageAfterInvocation(){
        return this.renderPageAfterInvocation;
    }
    public final Method getMethod(){
        return this.method;
    }
    public final String getName(){
        return this.name;
    }
    public final void invoke(final IRequestableComponent rcomponent){
        final Component component=(Component)rcomponent;
        if(!component.canCallListenerInterface(this.method)){
            RequestListenerInterface.log.info("component not enabled or visible; ignoring call. Component: "+component);
            throw new ListenerInvocationNotAllowedException(this,component,null,"Component rejected interface invocation");
        }
        this.internalInvoke(component,component);
    }
    public final void invoke(final IRequestableComponent rcomponent,final Behavior behavior){
        final Component component=(Component)rcomponent;
        if(!behavior.canCallListenerInterface(component,this.method)){
            RequestListenerInterface.log.warn("behavior not enabled; ignore call. Behavior {} at component {}",behavior,component);
            throw new ListenerInvocationNotAllowedException(this,component,behavior,"Behavior rejected interface invocation. ");
        }
        this.internalInvoke(component,behavior);
    }
    private void internalInvoke(final Component component,final Object target){
        final Boolean frozen=null;
        final Page page=component.getPage();
        while(true){
            if(!page.isInitialized()){
                page.internalInitialize();
                try{
                    this.method.invoke(target,new Object[0]);
                }
                catch(InvocationTargetException e){
                    if(e.getTargetException() instanceof RequestHandlerStack.ReplaceHandlerException||e.getTargetException() instanceof AuthorizationException||e.getTargetException() instanceof WicketRuntimeException){
                        throw (RuntimeException)e.getTargetException();
                    }
                    throw new WicketRuntimeException("Method "+this.method.getName()+" of "+this.method.getDeclaringClass()+" targeted at "+target+" on component "+component+" threw an exception",e);
                }
                catch(Exception e2){
                    throw new WicketRuntimeException("Method "+this.method.getName()+" of "+this.method.getDeclaringClass()+" targeted at "+target+" on component "+component+" threw an exception",e2);
                }
                finally{
                    if(frozen!=null){
                        page.setFreezePageId(frozen);
                    }
                }
                return;
            }
            continue;
        }
    }
    public void register(){
        this.registerRequestListenerInterface(this);
    }
    public String toString(){
        return "[RequestListenerInterface name="+this.name+", method="+this.method+"]";
    }
    private void registerRequestListenerInterface(final RequestListenerInterface requestListenerInterface){
        final RequestListenerInterface existingInterface=forName(requestListenerInterface.getName());
        if(existingInterface!=null){
            if(!existingInterface.getMethod().equals(requestListenerInterface.getMethod())){
                throw new IllegalStateException("Cannot register listener interface "+requestListenerInterface+" because it conflicts with the already registered interface "+existingInterface);
            }
        }
        else{
            RequestListenerInterface.interfaces.put(requestListenerInterface.getName(),requestListenerInterface);
            RequestListenerInterface.log.info("registered listener interface "+this);
        }
    }
    public static Collection<RequestListenerInterface> getRegisteredInterfaces(){
        return (Collection<RequestListenerInterface>)Collections.unmodifiableCollection(RequestListenerInterface.interfaces.values());
    }
    static{
        interfaces=Collections.synchronizedMap(new HashMap());
        log=LoggerFactory.getLogger(RequestListenerInterface.class);
    }
}
